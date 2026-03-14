import os

BASE = os.path.expanduser("~/AndroidStudioProjects/badgr_bolt/app/src/main/java/com/badgr/orbreader")

# ── 1. InAppPurchaseManager.kt — make queryExistingPurchases public ───────────
iap_path = os.path.join(BASE, "billing/InAppPurchaseManager.kt")
with open(iap_path, "r") as f:
    content = f.read()

content = content.replace(
    "private suspend fun queryExistingPurchases()",
    "suspend fun queryExistingPurchases()"
)

with open(iap_path, "w") as f:
    f.write(content)
print(f"Updated: {iap_path}")

# ── 2. MainActivity.kt — add onResume restoration call ───────────────────────
main_path = os.path.join(BASE, "MainActivity.kt")
with open(main_path, "r") as f:
    main = f.read()

# Add lifecycle import
if "import androidx.lifecycle.lifecycleScope" not in main:
    main = main.replace(
        "import android.os.Bundle",
        "import android.os.Bundle\nimport androidx.lifecycle.lifecycleScope"
    )
if "import kotlinx.coroutines.launch" not in main:
    main = main.replace(
        "import android.os.Bundle",
        "import android.os.Bundle\nimport kotlinx.coroutines.launch"
    )

# Add onResume before the closing brace of the class
on_resume = """
    override fun onResume() {
        super.onResume()
        val purchaseManager = (application as OrbReaderApp).purchaseManager
        if (purchaseManager.isConnected.value) {
            lifecycleScope.launch { purchaseManager.queryExistingPurchases() }
        }
    }
"""

main = main.rstrip()
if "override fun onResume" not in main:
    # Insert before final closing brace of class
    main = main[:main.rfind("}")] + on_resume + "}\n"

with open(main_path, "w") as f:
    f.write(main)
print(f"Updated: {main_path}")
print("Patch 2.3.5 complete.")
