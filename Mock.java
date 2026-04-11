Good — this confirms exactly what I expected.

👉 You are in a locked-down RHEL (VMC2) environment
👉 lnav is NOT available in your yum repos
👉 And you’re not going to get it without admin/EPEL access

So stop trying to install it — it’s not going to work.

⸻

🔥 What you should do instead (real solution)

You don’t need lnav.
You can get 80–90% of the same value with bash only.

⸻

✅ Step 1 — Create a proper colored log function

Edit:

nano ~/.bashrc

Add this:

tlog() {
  tail -f "$@" | sed \
    -e 's/\(ERROR\)/\x1b[31m\1\x1b[0m/g' \
    -e 's/\(WARN\)/\x1b[33m\1\x1b[0m/g' \
    -e 's/\(INFO\)/\x1b[32m\1\x1b[0m/g' \
    -e 's/\(Exception\)/\x1b[35m\1\x1b[0m/g'
}

Apply:

source ~/.bashrc


⸻

🚀 Usage

tlog app.log

👉 Now your logs are:
	•	ERROR → 🔴 red
	•	WARN → 🟡 yellow
	•	INFO → 🟢 green
	•	Exception → 🟣 purple

⸻

🔥 Step 2 — Make it even cleaner (optional but powerful)

Filter noise:

tlogf() {
  tail -f "$@" | grep --color=always -E "ERROR|WARN|INFO|Exception"
}


⸻

💣 Step 3 — Shortcut for your project

alias dglogs='tlog /opt/springboot/applications/dgvlm-api/logs/app.log'

Now just:

dglogs


⸻

🧠 Reality check (important mindset)

In corporate infra like yours:
	•	❌ You don’t install tools freely
	•	❌ You don’t control repos
	•	✅ You adapt using shell

👉 This is exactly what senior engineers do.

⸻

👍 Bottom line

👉 lnav not found = expected
👉 Not a blocker
👉 Your bash solution is enough and production-safe

⸻

If you want next level:
	•	highlight stack traces differently
	•	collapse repeated logs
	•	show only failures in real-time

I can give you a clean setup for that too.