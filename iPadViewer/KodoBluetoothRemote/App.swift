import SwiftUI

@main
struct KodoBluetoothRemoteApp: App {
    @StateObject private var ble = BLECentral()
    var body: some Scene {
        WindowGroup { ContentView().environmentObject(ble) }
    }
}
