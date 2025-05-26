export interface UserVO {
  userId: number;
  username: string;
  nickname: string;
  avatarUrl: string;
  gender: string; // MALE, FEMALE, UNKNOWN
  major: string;
  grade: string;
  contactInfo: string;
  skillTags: string; // JSON字符串
  creditScore: number;
  status: string; // ACTIVE, INACTIVE, BANNED
  createdAt: string;
  updatedAt: string;
  roles: string[];
}
