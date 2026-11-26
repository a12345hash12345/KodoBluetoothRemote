# BLE protocol (prototype)

BLE service UUID: `7E570001-6A2B-4A12-9B7A-1D4C0D0A0001`

Characteristics:
- Video notify: `7E570002-6A2B-4A12-9B7A-1D4C0D0A0001`
- Control write: `7E570003-6A2B-4A12-9B7A-1D4C0D0A0001`
- Status read/notify: `7E570004-6A2B-4A12-9B7A-1D4C0D0A0001`

## Control messages
UTF-8 JSON:
- `{"type":"ping"}`
- `{"type":"stop"}`
- `{"type":"key","code":29,"down":true}`
- `{"type":"key","code":29,"down":false}`
- `{"type":"tap","x":0.5,"y":0.5}`

Coordinates are normalized 0.0–1.0 relative to the captured screen.

## Video chunks
Each notification is one fragment:
- bytes 0–3: frame sequence number, unsigned big-endian
- bytes 4–5: fragment index, unsigned big-endian
- bytes 6–7: fragment count, unsigned big-endian
- bytes 8..N: fragment payload

This prototype uses the negotiated BLE MTU and chunks access units. The iOS side currently reports received frame fragments; full H.264 reassembly/VideoToolbox rendering is marked as the next integration task. BLE throughput is the major performance constraint.
