// 通知音效文件
import notificationSound from '@/assets/sounds/notification.mp3';
import messageSound from '@/assets/sounds/message.mp3';

// 播放通知声音
export const playNotificationSound = () => {
  try {
    const audio = new Audio(notificationSound);
    audio.volume = 0.5;
    audio.play().catch(error => {
      console.error('播放通知声音失败:', error);
    });
  } catch (error) {
    console.error('创建音频对象失败:', error);
  }
};

// 播放消息声音
export const playMessageSound = () => {
  try {
    const audio = new Audio(messageSound);
    audio.volume = 0.5;
    audio.play().catch(error => {
      console.error('播放消息声音失败:', error);
    });
  } catch (error) {
    console.error('创建音频对象失败:', error);
  }
};
