<template>
  <canvas ref="canvasRef"
    class="ocean"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request.js'
/**
 * Canvas 版「深海气泡墙」
 * 修改点：
 * - 使用 devicePixelRatio 做画布像素缩放，保持绘制在“CSS 像素”坐标系下
 * - 在画布尺寸变化时：保持气泡半径（视觉大小）不变（以 CSS 像素为单位）
 * - 在画布尺寸变化时：按比例调整气泡的位置（x,y），避免位置突变
 */

const canvasRef = ref(null)
let ctx
let rafId
let resizeObserver
let prevClientWidth = 0
let prevClientHeight = 0

// -------------------- 气泡数据 --------------------
// bubble: { id, x, y, r, vx, vy, text }
const bubbles = []

// -------------------- 工具方法 --------------------
function random(min, max) {
  return Math.random() * (max - min) + min
}

// -------------------- 调整画布像素尺寸并保持 CSS 像素坐标系 --------------------
function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  const clientW = canvas.clientWidth
  const clientH = canvas.clientHeight

  // 第一次记录尺寸
  if (prevClientWidth === 0) {
    prevClientWidth = clientW
    prevClientHeight = clientH
  }

  // 计算位置缩放比例（只缩放位置，不缩放半径 r）
  const scaleX = clientW / prevClientWidth || 1
  const scaleY = clientH / prevClientHeight || 1

  // 将现有气泡的位置按比例缩放，保持视觉上的相对布局
  for (const b of bubbles) {
    b.x *= scaleX
    b.y *= scaleY
    // 保持 b.r 不变（CSS 像素）
  }

  prevClientWidth = clientW
  prevClientHeight = clientH

  // 处理设备像素比，使绘制使用 CSS 像素为单位（视觉尺寸不受 backing store 改变影响）
  const dpr = window.devicePixelRatio || 1
  // 设置 backing store 大小（物理像素）
  canvas.width = Math.round(clientW * dpr)
  canvas.height = Math.round(clientH * dpr)
  // 将 Canvas 的绘图坐标系映射回 CSS 像素（这样后续所有绘制和坐标逻辑都用 clientWidth/clientHeight 的单位）
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

// -------------------- 新增气泡 --------------------
function addBubble(item) {
  const canvas = canvasRef.value
  if (!canvas) return
  // 使用 CSS 像素（clientWidth/clientHeight）来决定位置、半径等
  const r = random(22, 38)

  bubbles.push({
    id: item.id + '-' + Date.now(),
    text: item.content,
    r,
    x: random(r, canvas.clientWidth - r),
    y: canvas.clientHeight + r,
    vx: random(-0.2, 0.2),
    vy: random(0.6, 1.4)
  })
}

// -------------------- 物理更新 --------------------
function update() {
  const canvas = canvasRef.value
  if (!canvas) return

  for (let i = 0; i < bubbles.length; i++) {
    const b = bubbles[i]

    // ---------------- 上浮 ----------------
    b.y -= b.vy
    b.x += b.vx

    // ---------------- 左右边界 ----------------
    if (b.x < b.r) {
      b.x = b.r
      b.vx = Math.abs(b.vx)
    } else if (b.x > canvas.clientWidth - b.r) {
      b.x = canvas.clientWidth - b.r
      b.vx = -Math.abs(b.vx)
    }

    // ---------------- 碰撞（刚体分离） ----------------
    for (let j = i + 1; j < bubbles.length; j++) {
      const o = bubbles[j]

      let dx = b.x - o.x
      let dy = b.y - o.y
      let dist = Math.sqrt(dx * dx + dy * dy)
      const minDist = b.r + o.r

      if (dist === 0) {
        dx = Math.random() - 0.5
        dy = Math.random() - 0.5
        dist = Math.sqrt(dx * dx + dy * dy)
      }

      if (dist < minDist) {
        const nx = dx / dist
        const ny = dy / dist
        const overlap = minDist - dist

        b.x += nx * overlap * 0.5
        b.y += ny * overlap * 0.5
        o.x -= nx * overlap * 0.5
        o.y -= ny * overlap * 0.5
      }
    }
  }

  // ---------------- 顶部堆叠与销毁 ----------------
  const TOP_MARGIN = 20      // 顶部“水面”高度（CSS 像素）
  const MAX_TOP_COUNT = 5   // 最大堆叠数

  // 找出顶部区域的气泡并按 y 值排序（最上面的在前）
  const topBubbles = bubbles
    .filter(b => b.y - b.r <= TOP_MARGIN + b.r)
    .sort((a, b) => a.y - b.y)

  for (let i = bubbles.length - 1; i >= 0; i--) {
    const b = bubbles[i]

    // 到达顶部：停下参与堆叠
    if (b.y - b.r <= TOP_MARGIN) {
      b.y = TOP_MARGIN + b.r
      b.vy = 0
    }
  }

  // 如果顶部气泡过多，多余的继续上浮并销毁
  if (topBubbles.length > MAX_TOP_COUNT) {
    const excess = topBubbles.length - MAX_TOP_COUNT
    for (let i = 0; i < excess; i++) {
      const b = topBubbles[i]
      b.y -= 1.5 // 让气泡继续上浮
      if (b.y + b.r < -10) {
        const index = bubbles.findIndex(item => item.id === b.id)
        if (index > -1) bubbles.splice(index, 1)
      }
    }
  }
}


// -------------------- 绘制 --------------------
function draw() {
  const canvas = canvasRef.value
  if (!canvas) return

  // 因为我们对 ctx 做了 setTransform(dpr,0,0,dpr,0,0)，绘制和逻辑都使用 CSS 像素单位
  ctx.clearRect(0, 0, canvas.clientWidth, canvas.clientHeight)

  for (const b of bubbles) {
    // 内部
    ctx.beginPath()
    ctx.arc(b.x, b.y, b.r - 4, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(120,200,255,0.25)'
    ctx.fill()

    // 文本
    ctx.fillStyle = '#e6f7ff'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(b.text, b.x, b.y)
  }
}

// -------------------- 主循环 --------------------
function loop() {
  update()
  draw()
  rafId = requestAnimationFrame(loop)
}

// -------------------- 生命周期 --------------------
onMounted(() => {
  createSocket()
  const canvas = canvasRef.value
  ctx = canvas.getContext('2d')

  // 初始化画布尺寸和坐标变换
  resizeCanvas()

  // 监听画布尺寸变化（更可靠于 window.resize 的方法）
  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(resizeCanvas)
    resizeObserver.observe(canvas)
  } else {
    // 退回到 window resize
    window.addEventListener('resize', resizeCanvas)
  }

  // 初始 mock 数据
  // Array.from({ length: 10 }).forEach((_, i) => {
  //   addBubble({ id: i + 1, text: `气泡 ${i + 1}` })
  // })

  getgetApprove()



  loop()

  // WebSocket mock
  // const intervalId = setInterval(() => {
  //   addBubble({
  //     id: Math.random().toString(36).slice(2),
  //     text: '新气泡'
  //   })
  // }, 200)

  // 保留 intervalId，以便卸载时清理
  onUnmounted(() => {
    clearInterval(intervalId)
  })
})


const getgetApprove = () => {
  request.get('/messages/getApprove').then((res) => {
    console.log(res.data);
    res.data.forEach((item) => {
      addBubble(item)
    })
  })
}

const ready = ref(false)
let socket = null
function createSocket() {
  const socket = new WebSocket("ws://47.115.163.178:8080/ws/audit");

  socket.onopen = () => {
    console.log('✅ WebSocket 已连接')
    ready.value = true
    // 可选：第一次心跳
    socket.send(JSON.stringify({ type: 'ping' }))
  }

  socket.onmessage = e => {
    console.log('📨 收到消息', e.data)
    const data = JSON.parse(e.data);
    addBubble(data)
  }

  socket.onclose = e => {
    console.warn('❌ WebSocket 关闭', e.code, e.reason)
    ready.value = false
    // 这里可以写重连逻辑
  }

  socket.onerror = e => {
    console.error('WebSocket 异常', e)
  }
}

onUnmounted(() => {
  cancelAnimationFrame(rafId)
  if (resizeObserver) {
    resizeObserver.disconnect()
  } else {
    window.removeEventListener('resize', resizeCanvas)
  }
  socket && socket.close()
})
</script>

<style scoped>
.ocean {
  width: 100%;
  height: 100vh;
  display: block;
  background: radial-gradient(circle at bottom, #022c43, #021019);
}
</style>