import SwiftUI

struct ContentView: View {
    @EnvironmentObject var ble: BLECentral
    @State private var keyboardVisible = true
    @State private var pressed = Set<String>()

    private let rows: [[String]] = [
        ["Esc","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12"],
        ["` ~","1 !","2 @","3 #","4 $","5 %","6 ^","7 &","8 *","9 (","0 )","- _","= +","⌫"],
        ["Tab","Q","W","E","R","T","Y","U","I","O","P","[ {","] }","\\ |"],
        ["Caps","A","S","D","F","G","H","J","K","L","; :", "' \"","Return"],
        ["Shift","Z","X","C","V","B","N","M",", <",". >","/ ?","Shift"],
        ["Ctrl","Fn","Win","Alt","Space","Alt","←","↑","↓","→"]
    ]

    var body: some View {
        GeometryReader { geo in
            VStack(spacing: 10) {
                HStack {
                    VStack(alignment: .leading, spacing: 3) {
                        Text("KODO REMOTE").font(.headline).bold()
                        Text(ble.status).font(.caption).foregroundStyle(.secondary)
                    }
                    Spacer()
                    Button { ble.connect() } label: {
                        Label("Connect", systemImage: "dot.radiowaves.left.and.right")
                    }.buttonStyle(.borderedProminent)
                    Button { ble.send(["type":"stop"]); ble.disconnect() } label: {
                        Image(systemName: "stop.circle")
                    }.buttonStyle(.bordered)
                }

                // The viewer frame is intentionally 4:3. The actual portrait phone
                // image will be aspect-fitted here when the H.264 decoder is integrated.
                ZStack {
                    RoundedRectangle(cornerRadius: 16).fill(Color.black)
                    VStack(spacing: 8) {
                        Image(systemName: "iphone.gen3").font(.system(size: 42)).foregroundStyle(.white.opacity(0.7))
                        Text("4:3 SCREEN VIEWER").font(.caption).bold().foregroundStyle(.white)
                        Text("Waiting for video frames").font(.caption2).foregroundStyle(.white.opacity(0.6))
                        Text("BLE fragments: \(ble.receivedFragments)").font(.caption2).foregroundStyle(.white.opacity(0.5))
                    }
                }
                .frame(maxWidth: .infinity)
                .aspectRatio(4/3, contentMode: .fit)

                HStack {
                    Text("PC KEYBOARD").font(.caption).bold()
                    Spacer()
                    Button { withAnimation(.easeInOut(duration: 0.2)) { keyboardVisible.toggle() } } label: {
                        Image(systemName: keyboardVisible ? "keyboard.chevron.compact.down" : "keyboard")
                    }
                }

                if keyboardVisible {
                    VStack(spacing: 5) {
                        ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                            HStack(spacing: 4) {
                                ForEach(row, id: \.self) { key in
                                    KeyCap(title: key, isPressed: pressed.contains(key))
                                        .onLongPressGesture(minimumDuration: 0.45, pressing: { down in
                                            if down { pressed.insert(key) } else { pressed.remove(key) }
                                            sendKey(key, down: down)
                                        }, perform: {})
                                        .simultaneousGesture(TapGesture().onEnded {
                                            sendKey(key, down: true)
                                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.08) { sendKey(key, down: false) }
                                        })
                                }
                            }
                        }
                    }
                    .frame(maxHeight: .infinity)
                }
            }
            .padding(12)
            .background(Color(uiColor: .systemBackground))
        }
    }

    private func sendKey(_ key: String, down: Bool) {
        let map: [String: Int] = ["A":29,"B":30,"C":31,"D":32,"E":33,"F":34,"G":35,"H":36,"I":37,"J":38,"K":39,"L":40,"M":41,"N":42,"O":43,"P":44,"Q":45,"R":46,"S":47,"T":48,"U":49,"V":50,"W":51,"X":52,"Y":53,"Z":54,"Space":62,"Return":66,"Esc":111,"Tab":61,"⌫":67,"←":21,"↑":19,"→":22,"↓":20}
        if let code = map[key] { ble.send(["type":"key","code":code,"down":down]) }
    }
}

private struct KeyCap: View {
    let title: String
    let isPressed: Bool
    var body: some View {
        Text(title)
            .font(.system(size: 10, weight: .semibold, design: .rounded))
            .lineLimit(1).minimumScaleFactor(0.6)
            .frame(maxWidth: .infinity, minHeight: 30)
            .background(isPressed ? Color.accentColor.opacity(0.35) : Color.clear)
            .overlay(RoundedRectangle(cornerRadius: 5).stroke(Color.secondary.opacity(0.55), lineWidth: 1))
            .contentShape(Rectangle())
    }
}
