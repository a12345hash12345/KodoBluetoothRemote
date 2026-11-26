import Foundation
import CoreBluetooth

final class BluetoothManager: NSObject, ObservableObject,
                              CBCentralManagerDelegate, CBPeripheralDelegate {

    @Published var status = "Bluetooth starting..."
    @Published var devices: [CBPeripheral] = []
    @Published var connectedName: String?

    private var central: CBCentralManager!

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: .main)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            status = "Bluetooth ready"
        case .poweredOff:
            status = "Turn on Bluetooth"
        case .unauthorized:
            status = "Bluetooth permission denied"
        case .unsupported:
            status = "Bluetooth LE not supported"
        case .resetting:
            status = "Bluetooth resetting..."
        case .unknown:
            status = "Bluetooth state unknown"
        @unknown default:
            status = "Bluetooth unavailable"
        }
    }

    func scan() {
        guard central.state == .poweredOn else {
            status = "Turn on Bluetooth first"
            return
        }

        devices.removeAll()
        connectedName = nil
        status = "Scanning for devices..."
        central.scanForPeripherals(withServices: nil,
                                   options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
    }

    func stopScan() {
        central.stopScan()
        status = "Scan stopped"
    }

    func connect(_ peripheral: CBPeripheral) {
        central.stopScan()
        status = "Connecting..."
        central.connect(peripheral, options: nil)
    }

    func centralManager(_ central: CBCentralManager,
                        didDiscover peripheral: CBPeripheral,
                        advertisementData: [String: Any],
                        rssi RSSI: NSNumber) {
        guard !devices.contains(where: { $0.identifier == peripheral.identifier }) else {
            return
        }
        devices.append(peripheral)
    }

    func centralManager(_ central: CBCentralManager,
                        didConnect peripheral: CBPeripheral) {
        connectedName = peripheral.name ?? "Bluetooth device"
        status = "Connected: \(connectedName!)"
        peripheral.delegate = self
        peripheral.discoverServices(nil)
    }

    func centralManager(_ central: CBCentralManager,
                        didFailToConnect peripheral: CBPeripheral,
                        error: Error?) {
        status = "Connection failed"
    }

    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?) {
        connectedName = nil
        status = "Disconnected"
    }
}
