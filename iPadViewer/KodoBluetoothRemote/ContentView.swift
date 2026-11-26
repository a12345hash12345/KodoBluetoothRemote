import SwiftUI
import CoreBluetooth

struct ContentView: View {
    @EnvironmentObject var ble: BLECentral

    var body: some View {
        VStack(spacing: 20) {
            Text("Kodo Bluetooth Remote")
                .font(.largeTitle.bold())

            Text("Connect your Android device")
                .foregroundColor(.secondary)

            Button("Scan Bluetooth Devices") {
                ble.connect()
            }
            .buttonStyle(.borderedProminent)

            Button("Stop Scan") {
                ble.disconnect()
            }
            .buttonStyle(.bordered)

            Spacer()

            Text("KodoTechnology")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
    }
}
