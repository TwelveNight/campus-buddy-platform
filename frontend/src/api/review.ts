import request from '../utils/request';

// 评价对象的类型定义
export interface Review {
  reviewId?: number;
  reviewedUserId: number;
  reviewerUserId: number;
  relatedInfoId: number;
  score: number;
  content?: string;
  createdAt?: string;
  reviewerNickname?: string;
  reviewerAvatar?: string;
  moduleType?: string; // 例如：'互助', '学习小组', '资源共享'
}



// 评价查询参数
export interface ReviewQuery {
  userId?: number;      // 相关用户ID
  type?: string;        // 'received' 或 'given' - 收到的或发出的评价
  score?: number;       // 按评分筛选
  moduleType?: string;  // 模块类型筛选
  page?: number;        // 页码
  size?: number;        // 每页条数
}

// 提交评价
export function submitReview(review: Review) {
  return request({
    url: '/api/review/submit',
    method: 'post',
    data: review
  });
}

// 获取指定互助的评价
export function getReviewsByHelpId(helpId: number) {
  return request({
    url: `/api/review/byHelp/${helpId}`,
    method: 'get'
  });
}

// 获取用户收到的评价
export function getReviewsByUserId(userId: number) {
  return request({
    url: `/api/review/byUser/${userId}`,
    method: 'get'
  });
}

// 获取用户信用分
export function getUserCreditScore(userId: number) {
  return request({
    url: `/api/review/credit/${userId}`,
    method: 'get'
  });
}

// 检查用户是否已经评价过某互助
export function hasReviewed(reviewerId: number, helpInfoId: number) {
  return request({
    url: '/api/review/check',
    method: 'get',
    params: { reviewerId, helpInfoId }
  });
}

// 获取用户的评价列表（可筛选）
export function getUserReviews(query: ReviewQuery) {
  return request({
    url: '/api/review/list',
    method: 'get',
    params: query
  });
}


