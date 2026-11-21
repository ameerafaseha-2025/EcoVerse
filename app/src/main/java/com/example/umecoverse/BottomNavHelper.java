
package com.example.umecoverse;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class BottomNavHelper {

    public static void setup(Activity activity) {

        navigate(activity, R.id.navHome, HomeActivity.class);
        navigate(activity, R.id.navRanks, RanksActivity.class);
        navigate(activity, R.id.navScan, ScanActivity.class);
        navigate(activity, R.id.navRewards, RewardsActivity.class);
        navigate(activity, R.id.navProfile, ProfileActivity.class);
    }

    private static void navigate(
            Activity activity,
            int viewId,
            Class<? extends Activity> destination
    ) {

        View button = activity.findViewById(viewId);

        if (button == null) {
            return;
        }

        button.setOnClickListener(v -> {

            // Don't reopen the current page
            if (activity.getClass().equals(destination)) {
                return;
            }

            Intent intent = new Intent(activity, destination);

            // Avoid stacking duplicate main pages
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            activity.startActivity(intent);
        });
    }
}
