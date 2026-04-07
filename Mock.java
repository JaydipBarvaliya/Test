Good — this is exactly what I expected. You’re very close now.

⸻

🔍 What your screenshots show

You ran:

ssh dgvlm-dev

And got:

TAF5028@10.53.148.25's password:
Permission denied, please try again.

Then it eventually logs in after password.

👉 Translation:
	•	✅ Username is correct
	•	❌ SSH key is NOT being used
	•	❌ Server doesn’t recognize your key

⸻

🚨 Root cause (no guessing)

Your key exists locally, but:

👉 It is NOT added to the server’s authorized_keys

⸻

✅ Fix this properly (this is the missing piece)

Step 1 — Copy your public key (on WSL)

cat ~/.ssh/id_ed25519.pub

Copy the entire output.

⸻

Step 2 — Login to server (you already can)

ssh TAF5028@10.53.148.25

Enter password.

⸻

Step 3 — On server, add key

mkdir -p ~/.ssh
chmod 700 ~/.ssh
nano ~/.ssh/authorized_keys

👉 Paste your key here
👉 Save

Then run:

chmod 600 ~/.ssh/authorized_keys


⸻

Step 4 — Test again from WSL

ssh dgvlm-dev


⸻

🎯 Expected result

You should go from:

password prompt ❌

to:

direct login ✅


⸻

⚠️ One subtle issue in your setup

You still have:

StrictHostKeyChecking no
UserKnownHostsFile /dev/null

👉 Remove them now. They are useless at this point.

Clean config:

Host dgvlm-dev
    HostName 10.53.148.25
    User TAF5028
    IdentityFile ~/.ssh/id_ed25519


⸻

🧠 Why password worked but key didn’t

SSH tries:
	1.	Key auth ❌ (fails → not on server)
	2.	Falls back to password ✅

⸻

💡 Quick verification (optional but sharp)

Run:

ssh -v dgvlm-dev

Look for:

Offering public key: ~/.ssh/id_ed25519

If you don’t see it → config issue
If you see it but rejected → key not on server

⸻

Bottom line

You’ve done everything right except one step:

👉 You forgot to register your public key on the server

Do that, and you’re done.

⸻

If after adding key it still asks password, send me:

ssh -v dgvlm-dev

I’ll pinpoint the exact failure in seconds.