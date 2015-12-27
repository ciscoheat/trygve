;
; Keyboard shortcuts for compiling in trygve
; Use with https://autohotkey.com/
;
; F5  - Run
; F8  - Compile
; F9  - Compile + Run
; Tab - 4 spaces
;
#SingleInstance, force
#IfWinActive, trygve

Tab::SendInput {Space 4}
F8::ClickAt(ParseButtonPos(), 77)
F5::ClickAt(RunButtonPos(), 142)
F9::
ClickAt(ParseButtonPos(), 77)
ClickAt(RunButtonPos(), 142)
return

ParseButtonPos()
{
	return 444
}

RunButtonPos()
{
	; Calculating based on width of the window
	WinGetPos,,, width
	return width / 2 + 100
}

ClickAt(x, y)
{
	MouseGetPos, ox, oy
	MouseMove, %x%, %y%, 0
	Click
	MouseMove, %ox%, %oy%, 0	
}
