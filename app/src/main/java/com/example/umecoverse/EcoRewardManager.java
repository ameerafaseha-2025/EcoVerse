
package com.example.umecoverse;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class EcoRewardManager {

    private static final String TIME_ZONE = "Asia/Kuala_Lumpur";
    private static final int BASE_POINTS = 10;
    private static final int STREAK_REQUIREMENT = 5;
    private static final double STREAK_MULTIPLIER = 1.5;

    private final FirebaseFirestore db;
    private final DocumentReference userRef;
    private final DocumentReference leaderboardRef;

    public EcoRewardManager(String userId) {
        db = FirebaseFirestore.getInstance();

        userRef = db.collection("users").document(userId);
        leaderboardRef = db.collection("leaderboard").document(userId);
    }

    public static class RewardResult {

        public final int pointsEarned;
        public final int totalPoints;
        public final int streak;
        public final boolean streakIncreased;
        public final boolean multiplierApplied;

        public RewardResult(
                int pointsEarned,
                int totalPoints,
                int streak,
                boolean streakIncreased,
                boolean multiplierApplied
        ) {
            this.pointsEarned = pointsEarned;
            this.totalPoints = totalPoints;
            this.streak = streak;
            this.streakIncreased = streakIncreased;
            this.multiplierApplied = multiplierApplied;
        }
    }

    public Task<RewardResult> awardSuccessfulScan() {

        LocalDate today = LocalDate.now(ZoneId.of(TIME_ZONE));
        String todayString = today.toString();

        return db.runTransaction(transaction -> {

            DocumentSnapshot document = transaction.get(userRef);

            if (!document.exists()) {
                throw new IllegalStateException("User profile not found.");
            }

            Long pointsValue = document.getLong("ecoPoints");
            Long streakValue = document.getLong("streak");

            int totalPoints = pointsValue != null
                    ? pointsValue.intValue() : 0;

            int currentStreak = streakValue != null
                    ? streakValue.intValue() : 0;

            String lastScanDate = document.getString("lastScanDate");

            boolean streakIncreased = false;

            if (lastScanDate == null || lastScanDate.isEmpty()) {

                currentStreak = 1;
                streakIncreased = true;

            } else if (!lastScanDate.equals(todayString)) {

                LocalDate previousDate = LocalDate.parse(lastScanDate);

                if (previousDate.plusDays(1).equals(today)) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }

                streakIncreased = true;
            }

            boolean multiplierApplied =
                    currentStreak >= STREAK_REQUIREMENT;

            int pointsEarned = multiplierApplied
                    ? (int) (BASE_POINTS * STREAK_MULTIPLIER)
                    : BASE_POINTS;

            totalPoints += pointsEarned;

            // Update user profile
            transaction.update(
                    userRef,
                    "ecoPoints", totalPoints,
                    "streak", currentStreak,
                    "lastScanDate", todayString
            );

            // Get user information
            String name = document.getString("name");
            String college = document.getString("college");

            if (name == null || name.trim().isEmpty()) {
                name = "Eco Warrior";
            }

            if (college == null || college.trim().isEmpty()) {
                college = "Not specified";
            }

            // Prepare leaderboard data
            Map<String, Object> leaderboardData = new HashMap<>();

            leaderboardData.put("name", name);
            leaderboardData.put("college", college);
            leaderboardData.put("ecoPoints", totalPoints);

            // Update leaderboard
            transaction.set(leaderboardRef, leaderboardData);

            return new RewardResult(
                    pointsEarned,
                    totalPoints,
                    currentStreak,
                    streakIncreased,
                    multiplierApplied
            );
        });
    }
}

