# UMEcoverse — UI Component ID Reference

This document lists IDs extracted from `app/src/main/res/layout/*.xml`, grouped by layout/activity. IDs are copied exactly from the supplied terminal output. Component purpose is inferred from naming; check XML and Java before integration. IDs are case-sensitive.

## `activity_college_battle.xml` — CollegeBattleActivity
| ID | Expected purpose |
|---|---|
| `btnBack` | Back navigation |
| `tvTotalCarbon` | Total carbon display |
| `layoutCollegeMap` | College map section/container |
| `btnViewCollegeMap` | Open college map |
| `tvRank1` | First rank label |
| `progressRank1` | First rank progress |
| `tvCarbonRank1` | First rank carbon display |
| `tvRank2` | Second rank label |
| `progressRank2` | Second rank progress |
| `tvCarbonRank2` | Second rank carbon display |
| `tvRank3` | Third rank label |
| `progressRank3` | Third rank progress |
| `tvCarbonRank3` | Third rank carbon display |
| `btnCreateChallenge` | Open challenge creation |

## `activity_college_map.xml` — CollegeMapActivity
No `android:id="@+id/..."` entries were found by the extraction command.

## `activity_create_challenge.xml` — CreateChallengeActivity
| ID | Expected purpose |
|---|---|
| `etChallengeTitle` | Challenge title input |
| `etChallengeDescription` | Challenge description input |
| `etChallengeTarget` | Challenge target input |
| `btnSelectDeadline` | Choose deadline |
| `btnSubmitChallenge` | Submit challenge |

## `activity_home.xml` — HomeActivity
| ID | Expected purpose |
|---|---|
| `btnBack` | Back navigation |
| `tvEcoPoints` | Eco Points display |
| `cardStreak` | Recycling streak card |
| `tvStreak` | Streak value |
| `progressStreak` | Streak progress |
| `tvEcoScore` | Eco Score display |
| `progressEcoScore` | Eco Score progress |
| `btnStartScan` | Open scan screen |
| `btnCollegeBattle` | Open college battle |
| `tvCarbonSaved` | Carbon saved display |
| `btnKnowledge` | Open knowledge screen |
| `navHome` | Home navigation |
| `navRanks` | Ranks navigation |
| `navScan` | Scan navigation |
| `navRewards` | Rewards navigation |
| `navProfile` | Profile navigation |

## `activity_knowledge.xml` — KnowledgeActivity
| ID | Expected purpose |
|---|---|
| `btnBack` | Back navigation |
| `tvQuizStatus` | Quiz status display |
| `btnStartQuiz` | Start quiz |
| `btnReadSolar` | Open solar article |
| `btnReadWaste` | Open waste article |
| `tvKnowledgeBadge` | Knowledge badge display |
| `tvChallengeAccess` | Challenge access/status display |

## `activity_main.xml` — MainActivity
| ID | Expected purpose |
|---|---|
| `etEmail` | Email input |
| `etPassword` | Password input |
| `btnLogin` | Login action |

## `activity_profile.xml` — ProfileActivity
| ID | Expected purpose |
|---|---|
| `btnBack` | Back navigation |
| `tvProfileName` | Profile name |
| `tvProfileEmail` | Profile email |
| `tvProfileLevel` | Profile level |
| `tvProfilePoints` | Profile points |
| `tvProfileStreak` | Profile streak |
| `tvPersonalCarbon` | Personal carbon display |
| `tvCarbonGoal` | Carbon goal display |
| `progressCarbonGoal` | Carbon goal progress |
| `tvCarbonPercentage` | Carbon goal percentage |
| `tvProfileBadges` | Profile badges |
| `tvProfileCollege` | Residential college |
| `tvProfileCollegeRank` | College rank |

## `activity_ranks.xml` — RanksActivity
| ID | Expected purpose |
|---|---|
| `main` | Root/main layout |

## `activity_rewards.xml` — RewardsActivity
| ID | Expected purpose |
|---|---|
| `btnBack` | Back navigation |
| `tvRewardPoints` | Reward points |
| `tvRewardStreak` | Reward streak |
| `btnRedeemCafe` | Redeem cafe reward |
| `btnRedeemFoodcourt` | Redeem food court reward |
| `tvSprintTitle` | Sprint title |
| `tvSprintTime` | Sprint time |
| `tvSprintDescription` | Sprint description |
| `progressSprint` | Sprint progress |
| `tvSprintProgress` | Sprint progress text |
| `btnJoinSprint` | Join sprint |
| `tvRewardBadges` | Reward badges |

## `activity_scan.xml` — ScanActivity
| ID | Expected purpose |
|---|---|
| `imgPreview` | Selected image preview |
| `tvImageStatus` | Image status |
| `btnChoosePhoto` | Choose photo |
| `btnScanVerify` | Verify scan |

## `dialog_scan_error.xml` — Scan error dialog
| ID | Expected purpose |
|---|---|
| `tvErrorIcon` | Error icon/indicator |
| `tvErrorTitle` | Error title |
| `tvErrorSubtitle` | Error subtitle |
| `tvErrorMessage` | Error message |
| `btnRetryScan` | Retry scan |
| `tvNoPoints` | No-points notice |

## `dialog_scan_success.xml` — Scan success dialog
| ID | Expected purpose |
|---|---|
| `tvDetectedMaterial` | Detected material |
| `tvConfidence` | Classification confidence |
| `tvPointsEarned` | Points earned |
| `tvMultiplier` | Points multiplier |
| `tvEcoFact` | Eco fact |
| `btnScanContinue` | Continue after scan |

## Team integration notes
- Find these IDs in XML under `app/src/main/res/layout/` and reference them in Java with `R.id.<ID>` (e.g., `R.id.tvEcoPoints`).
- IDs are **case-sensitive**. Do not rename them without coordinating with developers using them.
- The same ID (such as `btnBack`) can be reused in different activity layouts; look it up in the correct inflated view/activity.
- This inventory includes only `android:id="@+id/..."` values in the listed layout XML files. It does not cover IDs declared using `@id/...`, IDs in other resources, or IDs created programmatically.
- This inventory is a naming reference, not a guarantee that event listeners, backend integration, or app behavior are implemented.
