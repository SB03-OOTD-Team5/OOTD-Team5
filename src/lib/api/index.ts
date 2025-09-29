// API Client
export { apiClient, ApiClient } from './client';

// Types
export * from './types';

// Auth API
export * as authApi from './auth';

// User API
export * as userApi from './users';

// Feed API
export * as feedApi from './feeds';

// Clothes API
export * as clothesApi from './clothes';

// Clothes Attributes API
export * as clothesAttributesApi from './clothes-attributes';

// Follow API
export * as followApi from './follows';

// Weather API
export * as weatherApi from './weather';

// Recommendation API
export * as recommendationApi from './recommendations';

// Notification API
export * as notificationApi from './notifications';

// Message API
export * as messageApi from './messages';

// Individual exports for convenience
export {
  // Auth
  signIn,
  signOut,
  refreshToken,
  resetPassword,
  getCsrfToken,
} from './auth';

export {
  // Users
  createUser,
  getUserList,
  getProfile,
  updateProfile,
  changePassword,
  updateRole,
  updateUserLock,
} from './users';

export {
  // Feeds
  getFeedList,
  createFeed,
  updateFeed,
  deleteFeed,
  likeFeed,
  unlikeFeed,
  getFeedComments,
  createFeedComment,
} from './feeds';

export {
  // Clothes
  getClothes,
  createClothes,
  updateClothes,
  deleteClothes,
  extractByUrl,
} from './clothes';

export {
  // Clothes Attributes
  getClothesAttributeDef,
  createClothesAttributeDef,
  updateClothesAttributeDef,
  deleteClothesAttributeDef,
} from './clothes-attributes';

export {
  // Follows
  createFollow,
  cancelFollow,
  getFollowSummary,
  getFollowings,
  getFollowers,
} from './follows';

export {
  // Weather
  getWeather,
} from './weather';

export {
  // Recommendations
  getRecommendation,
} from './recommendations';

export {
  // Notifications
  getNotifications,
  readNotification,
} from './notifications';

export {
  // Messages
  getDms,
} from './messages';
