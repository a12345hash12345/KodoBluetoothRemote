import SwiftUI
import CoreBluetooth

struct ContentView: View {
    @StateObject private var bluetooth = BluetoothManager()

    var body: some View {
        VStack(spacing: 18) {
            Text("Kodo Bluetooth Remote")
                .font(.largeTitle.bold())

            Text(bluetooth.status)
                .font(.headline)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)

            HStack(spacing: 12) {
                Button("Scan") {
                    bluetooth.scan()
                }
                .buttonStyle(.borderedProminent)

                Button("Stop Scan") {
                    bluetooth.stopScan()
                }
                .buttonStyle(.bordered)
            }

            if let name = bluetooth.connectedName {
                Text("Connected device: \(name)")
                    .font(.headline)
                    .foregroundColor(.green)
            }

            Divider()

            Text("Nearby Bluetooth Devices")
                .font(.title3.bold())

            List(bluetooth.devices, id: \.identifier) { device in
                Button {
                    bluetooth.connect(device)
                } label: {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(device.name ?? "Unknown device")
                            .font(.headline)
                        Text(device.identifier.uuidString)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }

            Spacer(minLength: 0)

            Text("KodoTechnology")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .frame(minWidth: 600, minHeight: 450)
    }
}
