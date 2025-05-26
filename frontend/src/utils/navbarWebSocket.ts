/**
 * NavBar组件中使用的WebSocket处理功能
 * 包含WebSocket通知和消息处理，以及连接管理
 */

import { ref } from 'vue';
import webSocketService from '@/utils/websocket';
import { getUnreadMessageCount } from '@/api/message';
import { getUnreadNotificationCount } from '@/api/notification';

/**
 * 为NavBar组件设置WebSocket处理
 * @param {Object} options - 配置选项
 * @param {Function} options.fetchRecentNotifications - 获取最近通知的方法
 * @param {Number} options.userId - 当前用户ID
 * @returns {Object} - 返回处理方法和状态
 */
export const setupNavBarWebSocket = (options) => {
  const { fetchRecentNotifications, userId } = options;
  const unreadMessageCount = ref(0);
  const unreadNotificationCount = ref(0);

  // 获取未读消息数量
  const fetchUnreadMessageCount = async () => {
    try {
      const res = await getUnreadMessageCount();
      if (res.data.code === 200) {
        unreadMessageCount.value = res.data.data.count || 0;
      }
    } catch (error) {
      console.error('获取未读消息数量失败', error);
    }
  };

  // 获取未读通知数量
  const fetchUnreadNotificationCount = async () => {
    try {
      const res = await getUnreadNotificationCount();
      if (res.data.code === 200) {
        unreadNotificationCount.value = res.data.data.count || 0;
      }
    } catch (error) {
      console.error('获取未读通知数量失败', error);
    }
  };

  // 处理WebSocket收到的通知
  const handleWebSocketNotification = (data) => {
    if (!data || data.type !== 'NOTIFICATION') return;
    
    // 更新未读通知计数
    fetchUnreadNotificationCount();
    
    // 如果通知下拉菜单是打开的，更新通知列表
    if (document.querySelector('.notification-dropdown')?.parentElement?.style.display !== 'none') {
      fetchRecentNotifications();
    }
    
    // 注意：声音通知已移除以解决兼容性问题
  };

  // 处理WebSocket收到的私信
  const handleWebSocketMessage = (data) => {
    if (!data || data.type !== 'PRIVATE_MESSAGE') return;
    
    // 仅更新未读消息计数，不弹窗
    fetchUnreadMessageCount();
    
    // 不在这里处理任何弹窗通知，避免重复
  };

  // 初始化WebSocket连接
  const initWebSocket = () => {
    if (userId) {
      // 连接WebSocket
      webSocketService.connect(userId);
      
      // 添加通知监听器
      webSocketService.addNotificationListener(handleWebSocketNotification);
      
      // 添加消息监听器
      webSocketService.addMessageListener(handleWebSocketMessage);
      
      // 添加连接状态监听器
      webSocketService.addConnectionListener((status) => {
        console.log('WebSocket连接状态:', status ? '已连接' : '已断开');
      });
    }
  };

  // 清理WebSocket连接
  const cleanupWebSocket = () => {
    webSocketService.disconnect();
  };

  return {
    unreadMessageCount,
    unreadNotificationCount,
    fetchUnreadMessageCount,
    fetchUnreadNotificationCount,
    initWebSocket,
    cleanupWebSocket
  };
};
