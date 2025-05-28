<template>
    <div class="emoji-picker">
        <el-popover
            placement="top-start"
            :width="350"
            trigger="click"
            popper-class="emoji-popover"
        >
            <template #reference>
                <el-button type="text" class="emoji-trigger">
                    <el-icon><Position /></el-icon>
                </el-button>
            </template>
            
            <div class="emoji-content">
                <div class="emoji-tabs">
                    <el-tabs v-model="activeTab" class="emoji-tabs-container">
                        <el-tab-pane label="😀" name="smileys">
                            <div class="emoji-grid">
                                <span 
                                    v-for="emoji in smileysEmojis" 
                                    :key="emoji"
                                    class="emoji-item"
                                    @click="selectEmoji(emoji)"
                                    :title="getEmojiTitle(emoji)"
                                >
                                    {{ emoji }}
                                </span>
                            </div>
                        </el-tab-pane>
                        
                        <el-tab-pane label="👋" name="gestures">
                            <div class="emoji-grid">
                                <span 
                                    v-for="emoji in gesturesEmojis" 
                                    :key="emoji"
                                    class="emoji-item"
                                    @click="selectEmoji(emoji)"
                                    :title="getEmojiTitle(emoji)"
                                >
                                    {{ emoji }}
                                </span>
                            </div>
                        </el-tab-pane>
                        
                        <el-tab-pane label="❤️" name="hearts">
                            <div class="emoji-grid">
                                <span 
                                    v-for="emoji in heartsEmojis" 
                                    :key="emoji"
                                    class="emoji-item"
                                    @click="selectEmoji(emoji)"
                                    :title="getEmojiTitle(emoji)"
                                >
                                    {{ emoji }}
                                </span>
                            </div>
                        </el-tab-pane>
                        
                        <el-tab-pane label="📱" name="objects">
                            <div class="emoji-grid">
                                <span 
                                    v-for="emoji in objectsEmojis" 
                                    :key="emoji"
                                    class="emoji-item"
                                    @click="selectEmoji(emoji)"
                                    :title="getEmojiTitle(emoji)"
                                >
                                    {{ emoji }}
                                </span>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>
                
                <div class="recently-used" v-if="recentlyUsed.length > 0">
                    <div class="section-title">最近使用</div>
                    <div class="emoji-grid">
                        <span 
                            v-for="emoji in recentlyUsed" 
                            :key="emoji"
                            class="emoji-item"
                            @click="selectEmoji(emoji)"
                            :title="getEmojiTitle(emoji)"
                        >
                            {{ emoji }}
                        </span>
                    </div>
                </div>
            </div>
        </el-popover>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Position } from '@element-plus/icons-vue'

// 定义事件
const emit = defineEmits<{
    select: [emoji: string]
}>()

// 当前激活的标签页
const activeTab = ref('smileys')

// 最近使用的表情
const recentlyUsed = ref<string[]>([])

// 表情分类
const smileysEmojis = [
    '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃',
    '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙',
    '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔',
    '🤐', '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '🤥',
    '😔', '😕', '🙁', '☹️', '😣', '😖', '😫', '😩', '🥺', '😢',
    '😭', '😤', '😠', '😡', '🤬', '🤯', '😳', '🥵', '🥶', '😱'
]

const gesturesEmojis = [
    '👋', '🤚', '🖐️', '✋', '🖖', '👌', '🤏', '✌️', '🤞', '🤟',
    '🤘', '🤙', '👈', '👉', '👆', '🖕', '👇', '☝️', '👍', '👎',
    '👊', '✊', '🤛', '🤜', '👏', '🙌', '👐', '🤲', '🤝', '🙏',
    '✍️', '💅', '🤳', '💪', '🦾', '🦿', '🦵', '🦶', '👂', '🦻',
    '👃', '🧠', '🦷', '🦴', '👀', '👁️', '👅', '👄', '💋', '🩸'
]

const heartsEmojis = [
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
    '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '♥️',
    '💌', '💒', '💍', '💎', '🌹', '🌺', '🌸', '🌼', '🌻', '🌷'
]

const objectsEmojis = [
    '📱', '💻', '⌨️', '🖥️', '🖨️', '🖱️', '🖲️', '💽', '💾', '💿',
    '📀', '🎥', '🎬', '📷', '📸', '📹', '📼', '🔍', '🔎', '🕯️',
    '💡', '🔦', '🏮', '📔', '📕', '📖', '📗', '📘', '📙', '📚',
    '📓', '📒', '📃', '📜', '📄', '📰', '🗞️', '📑', '🔖', '🏷️',
    '💰', '💴', '💵', '💶', '💷', '💸', '💳', '🧾', '💹', '✉️'
]

// 表情标题映射
const emojiTitles: Record<string, string> = {
    '😀': '开心',
    '😃': '高兴',
    '😄': '大笑',
    '😁': '咧嘴笑',
    '😆': '眯眼笑',
    '😅': '汗笑',
    '🤣': '笑哭了',
    '😂': '喜极而泣',
    '🙂': '微笑',
    '🙃': '颠倒笑脸',
    '😉': '眨眼',
    '😊': '羞涩笑',
    '😇': '天使',
    '🥰': '爱心脸',
    '😍': '花痴',
    '🤩': '星星眼',
    '😘': '飞吻',
    '😗': '亲亲',
    '😚': '闭眼亲亲',
    '😙': '微笑亲亲',
    '😋': '美味',
    '😛': '吐舌头',
    '😜': '眨眼吐舌',
    '🤪': '疯狂脸',
    '😝': '眯眼吐舌',
    '🤑': '金钱脸',
    '🤗': '拥抱',
    '🤭': '捂嘴笑',
    '🤫': '嘘',
    '🤔': '思考',
    '👋': '挥手',
    '👍': '点赞',
    '👎': '点踩',
    '👏': '鼓掌',
    '🙏': '祈祷',
    '❤️': '红心',
    '💙': '蓝心',
    '💚': '绿心',
    '💛': '黄心',
    '💜': '紫心',
    '💔': '心碎',
    '💕': '两颗心',
    '💖': '闪亮心',
    '📱': '手机',
    '💻': '电脑',
    '📚': '书本',
    '💡': '灯泡',
    '🎉': '庆祝',
    '🎊': '彩带'
}

// 获取表情标题
const getEmojiTitle = (emoji: string): string => {
    return emojiTitles[emoji] || emoji
}

// 选择表情
const selectEmoji = (emoji: string) => {
    // 添加到最近使用
    addToRecentlyUsed(emoji)
    
    // 触发选择事件
    emit('select', emoji)
}

// 添加到最近使用
const addToRecentlyUsed = (emoji: string) => {
    // 移除已存在的
    const index = recentlyUsed.value.indexOf(emoji)
    if (index > -1) {
        recentlyUsed.value.splice(index, 1)
    }
    
    // 添加到开头
    recentlyUsed.value.unshift(emoji)
    
    // 只保留最近10个
    if (recentlyUsed.value.length > 10) {
        recentlyUsed.value = recentlyUsed.value.slice(0, 10)
    }
    
    // 保存到本地存储
    localStorage.setItem('chat-recently-used-emojis', JSON.stringify(recentlyUsed.value))
}

// 从本地存储加载最近使用的表情
const loadRecentlyUsed = () => {
    const stored = localStorage.getItem('chat-recently-used-emojis')
    if (stored) {
        try {
            recentlyUsed.value = JSON.parse(stored)
        } catch (error) {
            console.error('加载最近使用的表情失败:', error)
        }
    }
}

onMounted(() => {
    loadRecentlyUsed()
})
</script>

<style scoped>
.emoji-picker {
    display: inline-block;
}

.emoji-trigger {
    font-size: 18px;
    color: var(--text-secondary, #909399);
    padding: 8px;
    margin: 0;
}

.emoji-trigger:hover {
    color: var(--primary-color, #409eff);
}

.emoji-content {
    max-height: 300px;
    overflow-y: auto;
}

.emoji-tabs-container {
    margin-bottom: 10px;
}

.emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;
    padding: 10px 5px;
}

.emoji-item {
    font-size: 24px;
    cursor: pointer;
    padding: 6px;
    border-radius: 6px;
    text-align: center;
    transition: all 0.2s;
    user-select: none;
}

.emoji-item:hover {
    background-color: var(--el-fill-color-light);
    transform: scale(1.2);
}

.recently-used {
    border-top: 1px solid var(--el-border-color-lighter);
    padding-top: 10px;
}

.section-title {
    font-size: 12px;
    color: var(--text-secondary, #909399);
    margin-bottom: 8px;
    padding: 0 5px;
}

/* 暗色主题适配 */
[data-theme="dark"] .emoji-trigger {
    color: var(--dark-text-secondary, #a3a6ad);
}

[data-theme="dark"] .emoji-trigger:hover {
    color: var(--primary-color-dark, #60a9ff);
}

[data-theme="dark"] .emoji-item:hover {
    background-color: var(--dark-bg-hover, rgba(255, 255, 255, 0.05));
}

[data-theme="dark"] .recently-used {
    border-color: var(--dark-border-color, #4c4d4f);
}

[data-theme="dark"] .section-title {
    color: var(--dark-text-secondary, #a3a6ad);
}
</style>

<style>
.emoji-popover {
    padding: 10px !important;
}

.emoji-popover .el-tabs__header {
    margin: 0 0 10px 0;
}

.emoji-popover .el-tabs__item {
    font-size: 18px;
    padding: 8px 12px;
}

.emoji-popover .el-tabs__nav-wrap::after {
    display: none;
}
</style>
