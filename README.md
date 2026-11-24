# VEPEN — Android UI demo

Version 0.2.0 is an educational Persian Android interface prototype. It lists launcher-visible apps and stores selections locally, shows device-wide traffic counters when available, and checks Android's validated network status. The counters are approximate device-wide totals since opening the screen, not VPN traffic. There is no background service; counters refresh every five seconds only while visible.

No VPN tunnel, per-app routing, fixed IP, DNS changes, or server switching is implemented. No credentials are requested. No data is transmitted by this app.

Build via GitHub Actions (debug APK). This is not a production VPN.

## Version 0.3.0: Android local VPN lab

The local test requests Android's VPN consent, opens a temporary TUN interface restricted to this app and a single RFC 5737 documentation address, binds a synthetic UDP probe to the VPN network and verifies that its packet reaches the local TUN reader. It never forwards packets, changes DNS, routes other apps, or connects to an external server. The test stops automatically within seconds and refuses to start if an active VPN is detected. No remote encryption, server connection, static public IP, or real VPN traffic is verified. The previous Linux WireGuard lab remains separate. Device testing required.
