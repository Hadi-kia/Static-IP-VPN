# VEPEN — Android UI demo

Version 0.2.0 is an educational Persian Android interface prototype. It lists launcher-visible apps and stores selections locally, shows device-wide traffic counters when available, and checks Android's validated network status. The counters are approximate device-wide totals since opening the screen, not VPN traffic. There is no background service; counters refresh every five seconds only while visible.

No VPN tunnel, per-app routing, fixed IP, DNS changes, or server switching is implemented. No credentials are requested. No data is transmitted by this app.

Build via GitHub Actions (debug APK). This is not a production VPN.
