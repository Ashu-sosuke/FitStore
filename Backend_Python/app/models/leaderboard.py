from pydantic import BaseModel, Field
from typing import Optional

class UserProfile(BaseModel):
    userId: str
    friendCode: str
    displayName: str
    avatarInitials: str

class WorkoutPoints(BaseModel):
    userId: str
    points: int
    period: str # "WEEKLY", "MONTHLY", "ALL_TIME"
    steps: Optional[int] = 0
    workoutsCount: Optional[int] = 0


class AddFriendRequest(BaseModel):
    userId: str
    friendCode: str

class LeaderboardEntry(BaseModel):
    userId: str
    friendCode: str
    displayName: str
    avatarInitials: str
    weeklyPoints: int = 0
    workoutsThisWeek: int = 0
    currentStreak: int = 0
    steps: int = 0

