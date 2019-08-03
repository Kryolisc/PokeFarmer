# PokeFarmer
A very small tool to automatically spin Pokestops using the Android Debug Bridge

_______________________________________________________

## Use at your own risk!
_______________________________________________________

## Usage

1) Download and install the Android Debug Bridge (ADB) including the drivers necessary for your device
2) Enable USB-Debugging in your devices settings (For my test devices, no rooting was necessary)
3) Connect your device to your PC
4) (Optional) Check the connection by starting a terminal inside the ADB installation directory
    Run the command `adb devices` (for PowerShell `.\adb.exe devices`)
    If your device is listed, continue.
5) If everything works fine, you can run the program. Open another terminal where the .jar-file is located.
   Run `java -jar TheJarFile.jar` to execute the program with the default ADB installation directory, which is `C:\adb`.
   To specify the ADB installation directory, just append its path to the command as a parameter.
6) After a few seconds, the bot should do its job.

The bot will initially take a screenshot to determine your screen resolution. This way it can calculate the positions for all the touch/swipe commands itself.
The only things you need to take care of by now is that
  1) The Pokestop is reachable from your current location (you could use a Fake-GPS app if you don't have access to any Pokestops near you)
  2) Nothing blocks the spinning part of the Pokestop (like other apps or that "You are going too fast" message)
  3) Pokemon GO is in fullscreen
  4) Your inventory is not full
