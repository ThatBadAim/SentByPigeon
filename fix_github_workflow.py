import os
import glob

files = glob.glob(".github/workflows/*.yml")
if len(files) > 0:
    file_path = files[0]
    with open(file_path, "r") as f:
        content = f.read()

    # Need to update actions/checkout@v4 to v4 or v4 with Node 24 if possible,
    # But it says: Node.js 20 actions are deprecated... To opt into Node.js 24 now, set FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true.
    if "FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true" not in content:
        content = content.replace("ACTIONS_ALLOW_USE_UNSECURE_NODE_VERSION: true", "FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: true")

    with open(file_path, "w") as f:
        f.write(content)
