import Foundation
import CoreBluetooth

final class BLECentral: NSObject, ObservableObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    static let serviceID = CBUUID(string: "7E570001-6A2B-4A12-9B7A-1D4C0D0A0001")
    static let videoID = CBUUID(string: "7E570002-6A2B-4A12-9B7A-1D4C0D0A0001")
    static let controlID = CBUUID(string: "7E570003-6A2B-4A12-9B7A-1D4C0D0A0001")
    static let statusID = CBUUID(string: "7E570004-6A2B-4A12-9B7A-1D4C0D0A0001")

    @Published var status = "Tap Connect to scan"
    @Published var connected = false
    @Published var receivedFragments = 0
    private var central: CBCentralManager!
    private var peripheral: CBPeripheral?
    private var control: CBCharacteristic?

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: .main)
    }

    func connect() {
        guard central.state == .poweredOn else { status = "Turn on Bluetooth"; return }
        status = "Scanning for Android host…"
        central.scanForPeripherals(withServices: [Self.serviceID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
    }

    func disconnect() {
        if let p = peripheral { central.cancelPeripheralConnection(p) }
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        status = central.state == .poweredOn ? "Bluetooth ready" : "Bluetooth unavailable"
    }

    func centralManager(_ central: CBCentralManager, didDiscover p: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        peripheral = p
        central.stopScan()
        p.delegate = self
        central.connect(p, options: nil)
        status = "Connecting…"
    }

    func centralManager(_ central: CBCentralManager, didConnect p: CBPeripheral) {
        connected = true
        status = "Connected — discovering services"
        p.discoverServices([Self.serviceID])
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect p: CBPeripheral, error: Error?) {
        connected = false
        status = "Connection failed"
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral p: CBPeripheral, error: Error?) {
        connected = false
        control = nil
        status = "Disconnected"
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        peripheral.services?.forEach { peripheral.discoverCharacteristics([Self.videoID, Self.controlID, Self.statusID], for: $0) }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        service.characteristics?.forEach { c in
            if c.uuid == Self.controlID { control = c }
            if c.uuid == Self.videoID { peripheral.setNotifyValue(true, for: c) }
        }
        status = "Connected — video transport experimental"
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if characteristic.uuid == Self.videoID, characteristic.value != nil { receivedFragments += 1 }
    }

    func send(_ object: [String: Any]) {
        guard let p = peripheral, let c = control, let data = try? JSONSerialization.data(withJSONObject: object) else { return }
        p.writeValue(data, for: c, type: .withoutResponse)
    }
}
