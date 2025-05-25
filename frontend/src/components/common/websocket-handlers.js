// 获取未读消息数量
const fetchUnreadMessageCount = async () => {
    try {
        const res = await getUnreadMessageCount()
        if (res.data.code === 200) {
            unreadMessageCount.value = res.data.data.count || 0
        }
    } catch (error) {
        console.error('获取未读消息数量失败', error)
    }
}

// 处理WebSocket收到的通知
const handleWebSocketNotification = (data) => {
    if (!data || data.type !== 'NOTIFICATION') return

    // 更新未读通知计数
    fetchUnreadCount()

    // 如果通知下拉菜单是打开的，更新通知列表
    if (document.querySelector('.notification-dropdown')?.parentElement?.style.display !== 'none') {
        fetchRecentNotifications()
    }

    // 播放通知提示音（可选）
    import('@/utils/sound').then(({ playNotificationSound }) => {
        playNotificationSound()
    })
}

// 处理WebSocket收到的私信
const handleWebSocketMessage = (data) => {
    if (!data || data.type !== 'PRIVATE_MESSAGE') return

    // 更新未读消息计数
    fetchUnreadMessageCount()

    // 播放消息提示音（可选）
    import('@/utils/sound').then(({ playMessageSound }) => {
        playMessageSound()
    })
}

// 初始化WebSocket连接
const initWebSocket = () => {
    if (authStore.isAuthenticated && authStore.user?.userId) {
        // 连接WebSocket
        webSocketService.connect(authStore.user.userId)

        // 添加通知监听器
        webSocketService.addNotificationListener(handleWebSocketNotification)

        // 添加消息监听器
        webSocketService.addMessageListener(handleWebSocketMessage)

        // 添加连接状态监听器
        webSocketService.addConnectionListener((status) => {
            console.log('WebSocket连接状态:', status ? '已连接' : '已断开')
        })
    }
}

// 组件挂载时
onMounted(() => {
    // 设置通知轮询
    setupNotificationPolling()

    // 获取未读消息数量
    if (authStore.isAuthenticated) {
        fetchUnreadMessageCount()
    }

    // 初始化WebSocket连接
    initWebSocket()
})

// 组件卸载前
onBeforeUnmount(() => {
    clearNotificationPolling()

    // 断开WebSocket连接
    webSocketService.disconnect()
})
