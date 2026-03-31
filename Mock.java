Good — now we can see exactly what’s going on, and this is the last small gap.

⸻

🔍 What your screenshot proves

You ran:

echo $env:CONFIG

Output:

C
:
\
C
A
T
\
c
o
n
f
i
g
\
m
a
n
i
f
e
s
t
_
a
u
t
o
.
j
s
o
n

👉 That means:
CONFIG is set as a character array instead of a normal string

⸻

💥 Why this happens

Somewhere earlier, PowerShell interpreted your value incorrectly (likely from previous set usage or copy-paste weirdness).

So instead of:

C:\CAT\config\manifest_auto.json

It stored:

['C',':','\',...]


⸻

✅ Fix (reset clean — don’t patch)

Run this exactly:

Remove-Item Env:CONFIG

Now set it again properly:

$env:CONFIG = "C:\CAT\config\manifest_auto.json"


⸻

🧪 Verify (this time it must be ONE line)

echo $env:CONFIG

Expected:

C:\CAT\config\manifest_auto.json

👉 If it still prints vertically → still broken → stop and re-run

⸻

🚀 Now run the command

C:\Users\taf5028\AppData\Roaming\npm\certify-api.cmd pretest


⸻

⚠️ One more thing (important)

You also had this earlier:

set CONFIG=C:\CAT\config-key   ❌

👉 That overwrote CONFIG with wrong value.

Make sure:

$env:CONFIG_KEY = "C:\CAT\config-key\"


⸻

🧠 What just happened (lock this in)

You hit a rare but annoying PowerShell behavior:
	•	Mixing set + $env: → corrupts env var format
	•	Result → array instead of string

⸻

💡 Clean mental checklist

Before running:

echo $env:CONFIG
echo $env:CONFIG_KEY

If both look clean → command will work.

⸻

🚀 Bottom line

You’re not stuck anymore — you’re just cleaning up a bad env state.

⸻

Run this fix and send me the next output — we’re basically at the finish line 👍