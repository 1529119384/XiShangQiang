<template>
  <div class="center-container">
    <div class="card">
      <div class="top-row">
        <div class="greeting"
          v-if="isLoggedIn">
          已登录
        </div>
        <el-button plain
          @click="handleAuthButton">
          {{ authButtonText }}
        </el-button>
      </div>

      <el-input v-model="message"
        class="message-input"
        autosize
        type="textarea"
        placeholder="请输入内容" />
      <el-button type="primary"
        @click="sendMessage"
        class="send-button">
        发送消息
      </el-button>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegisterDialog"
      title="注册"
      :width="dialogWidth"
      :close-on-click-modal="false">
      <template #default>
        <el-form :model="form"
          label-width="auto"
          class="auth-form"
          :rules="registerRules"
          ref="registerFormRef">
          <el-form-item label="用户名"
            required
            prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码"
            required
            prop="password">
            <el-input v-model="form.password"
              type="password" />
          </el-form-item>
          <el-form-item label="姓名"
            required
            prop="nickname">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="职位"
            required
            prop="position">
            <el-input v-model="form.position" />
          </el-form-item>
          <el-form-item class="buttons-row">
            <el-button type="primary"
              @click="register"
              :disabled="!form.username || !form.password || !form.nickname || !form.position">注册</el-button>
            <el-button type="primary"
              @click="toLogin">前往登录</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>

    <!-- 登录弹窗 -->
    <el-dialog v-model="showLoginDialog"
      title="登录"
      :width="dialogWidth"
      :close-on-click-modal="false">
      <template #default>
        <el-form :model="form"
          label-width="auto"
          class="auth-form"
          :rules="loginRules"
          ref="loginFormRef">
          <el-form-item label="用户名"
            required
            prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="密码"
            required
            prop="password">
            <el-input v-model="form.password"
              type="password" />
          </el-form-item>
          <el-form-item class="buttons-row">
            <el-button type="primary"
              @click="login"
              :disabled="!form.username || !form.password">登录</el-button>
            <el-button type="primary"
              @click="toRegister">前往注册</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const showLoginDialog = ref(false)
const showRegisterDialog = ref(false)
const message = ref('')

// 表单引用
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 表单验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }]
}

// 表单共用（可以按需拆分为 loginForm / registerForm）
const form = ref({
  username: '',
  password: '',
  nickname: '',
  position: ''
})

// 响应式登录状态：根据 localStorage 判断
const isLoggedIn = ref(false)
const dialogWidth = ref('500px') // 默认对话框宽度，可根据屏幕调整

const getStoredLogin = () => {
  try {
    const raw = localStorage.getItem('loginUser')
    if (!raw) return null
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

onMounted(() => {
  const info = getStoredLogin()
  isLoggedIn.value = !!(info && info.token)

  // 如果是小屏幕，使用更小/百分比的 dialog 宽度（更适合手机）
  if (window.innerWidth <= 420) {
    dialogWidth.value = '90%'
  } else {
    dialogWidth.value = '500px'
  }
})

// 显示在按钮上的文字
const authButtonText = computed(() => (isLoggedIn.value ? '退出登录' : '登录'))

// 点击顶部按钮的统一处理：未登录则打开登录框，已登录则登出
const handleAuthButton = () => {
  if (isLoggedIn.value) {
    logout()
  } else {
    showLoginDialog.value = true
  }
}

const register = () => {
  request.post('/user/register', form.value).then((success) => {
    console.log('register res', success)
    // 检查响应数据是否成功
    if (success.message != 'REGISTER_FAILED') {
      // 注册后通常自动跳转到登录或直接登录，这里我们关闭注册框并清空密码
      form.value.password = ''
      showRegisterDialog.value = false
      ElMessage({
        message: '注册成功',
        type: 'success',
      })
    } else {
      // 注册失败
      ElMessage.error('注册失败' + (success.data || ''))
    }
  }).catch((err) => {
    console.error('register error', err)
    ElMessage.error('注册失败' + (success.data || ''))
  })
}

const login = () => {
  request.post('/user/login', form.value).then((success) => {
    console.log('login res', success)
    // 检查响应数据是否包含错误信息或token
    if (success.message != 'LOGIN_FAILED') {
      // 存储 token（和用户名以便 UI 展示）
      localStorage.setItem('loginUser', JSON.stringify({
        token: success.data,
      }))
      // 清空密码字段以安全处理
      form.value.password = ''
      showLoginDialog.value = false
      isLoggedIn.value = true
      ElMessage({
        message: '登录成功',
        type: 'success',
      })
    } else {
      // 没有token，登录失败
      ElMessage.error('登录失败' + (success.data || ''))
    }
  }).catch((err) => {
    console.error('login error', err)
    ElMessage.error('登录失败' + (success.data || ''))
  })
}

const logout = () => {
  // 清除本地登录信息并切换状态
  localStorage.removeItem('loginUser')
  isLoggedIn.value = false
  // 可选：清空表单和消息
  form.value.username = ''
  form.value.password = ''
  message.value = ''
}

const toRegister = () => {
  showLoginDialog.value = false
  // 微小延迟以保证 dialog 切换的动画不冲突
  setTimeout(() => {
    showRegisterDialog.value = true
  }, 100)
}
const toLogin = () => {
  showRegisterDialog.value = false
  setTimeout(() => {
    showLoginDialog.value = true
  }, 100)
}

const sendMessage = () => {
  if (!isLoggedIn.value) {
    ElMessage.primary('请先登录')
    return
  }
  request.post('/messages/submit', {
    content: message.value
  }).then((res) => {
    console.log(res)
    // 发送后清空输入并保持在中心布局
    message.value = ''
  }).catch((err) => {
    console.error('send message error', err)
  })
}
</script>

<style>
/* 中心容器：垂直居中，水平居中，适配手机 */
.center-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  background: #f5f7fa;
}

/* 卡片样式，容器内居中和响应式 */
.card {
  width: 100%;
  max-width: 720px;
  background: #fff;
  padding: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
  align-items: center;
}

/* 顶部按钮和问候语一行排列 */
.top-row {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

/* 当需要显示用户名或状态时 */
.greeting {
  margin-right: auto;
  color: #606266;
  font-size: 14px;
}

/* 文本输入和按钮宽度在较大屏幕上有限制 */
.message-input {
  width: 100%;
  max-width: 560px;
}

/* 发送按钮居中 */
.send-button {
  align-self: center;
}

/* 表单内宽度限制，更适配手机 */
.auth-form {
  width: 100%;
  max-width: 420px;
  box-sizing: border-box;
}

/* 弹窗内按钮居中显示 */
.buttons-row {
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 响应式：更小屏幕时的调整 */
@media (max-width: 420px) {
  .card {
    padding: 16px;
    max-width: 100%;
    border-radius: 6px;
  }

  .top-row {
    justify-content: space-between;
  }

  .message-input {
    max-width: 100%;
  }

  .auth-form {
    padding: 0 6px;
  }
}
</style>