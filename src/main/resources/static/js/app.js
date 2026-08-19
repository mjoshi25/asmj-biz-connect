// Attach CSRF Token Globally to all HTMX Requests
document.addEventListener("DOMContentLoaded", () => {
    document.body.addEventListener('htmx:configRequest', (event) => {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        if (tokenMeta && headerMeta) {
            const token = tokenMeta.getAttribute('content');
            const header = headerMeta.getAttribute('content');
            if (token && header) {
                event.detail.headers[header] = token;
            }
        }
    });

    initWebSockets();
});

// Global Modal Tab Switcher (Direct Message vs Create Group)
window.switchChatTab = function(tab) {
    const directBtn = document.getElementById('tab-direct-btn');
    const groupBtn = document.getElementById('tab-group-btn');
    const directContent = document.getElementById('tab-direct-content');
    const groupContent = document.getElementById('tab-group-content');

    if (!directBtn || !groupBtn || !directContent || !groupContent) return;

    if (tab === 'direct') {
        directBtn.className = 'flex-1 py-1.5 text-xs font-bold rounded-lg transition bg-white text-black shadow-sm';
        groupBtn.className = 'flex-1 py-1.5 text-xs font-bold rounded-lg transition text-gray-400 hover:text-white';
        directContent.classList.remove('hidden');
        groupContent.classList.add('hidden');
    } else {
        groupBtn.className = 'flex-1 py-1.5 text-xs font-bold rounded-lg transition bg-white text-black shadow-sm';
        directBtn.className = 'flex-1 py-1.5 text-xs font-bold rounded-lg transition text-gray-400 hover:text-white';
        groupContent.classList.remove('hidden');
        directContent.classList.add('hidden');
    }
};

// Global Live User Filter for Modals
window.filterUserList = function(inputId, listId) {
    const input = document.getElementById(inputId);
    const list = document.getElementById(listId);
    if (!input || !list) return;

    const query = input.value.trim().toLowerCase();
    const rows = list.querySelectorAll('.user-row');

    rows.forEach(row => {
        const nameEl = row.querySelector('.user-name');
        const handleEl = row.querySelector('.user-handle');
        const name = nameEl ? nameEl.textContent.toLowerCase() : '';
        const handle = handleEl ? handleEl.textContent.toLowerCase() : '';

        if (name.includes(query) || handle.includes(query)) {
            row.style.setProperty('display', 'flex', 'important');
        } else {
            row.style.setProperty('display', 'none', 'important');
        }
    });
};

// Global Message Attachment Preview & Reset
window.previewMsgAttachment = function(input) {
    const file = input.files[0];
    if (!file) return;

    const wrapper = document.getElementById('msg-file-preview-wrapper');
    const fileName = document.getElementById('msg-file-name');
    const imgPreview = document.getElementById('msg-image-preview');
    const fileIcon = document.getElementById('msg-file-icon');

    if (!wrapper || !fileName || !imgPreview || !fileIcon) return;

    fileName.textContent = file.name;
    wrapper.classList.remove('hidden');

    if (file.type && file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = function(e) {
            imgPreview.src = e.target.result;
            imgPreview.classList.remove('hidden');
            fileIcon.classList.add('hidden');
        };
        reader.readAsDataURL(file);
    } else {
        imgPreview.classList.add('hidden');
        fileIcon.classList.remove('hidden');
    }
};

window.clearMsgAttachment = function() {
    const input = document.getElementById('msg-file-input');
    if (input) input.value = '';
    const wrapper = document.getElementById('msg-file-preview-wrapper');
    if (wrapper) wrapper.classList.add('hidden');
    const imgPreview = document.getElementById('msg-image-preview');
    if (imgPreview) imgPreview.src = '#';
};

window.scrollMessagesToBottom = function() {
    const container = document.getElementById('messages-container');
    if (container) {
        container.scrollTop = container.scrollHeight;
    }
};

// Interactive Toast Notification Manager
function showToast(title, subtitle, snippet, url) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toastId = 'toast-' + Math.random().toString(36).substring(2, 9);
    const toastHtml = `
        <div id="${toastId}" 
             class="pointer-events-auto bg-gray-950/95 border border-indigo-500/40 text-white p-4 rounded-2xl shadow-2xl flex items-start space-x-3 transform transition-all duration-300 translate-x-full opacity-0 backdrop-blur-xl">
            <div class="w-8 h-8 rounded-full bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center font-bold text-xs shrink-0 mt-0.5 shadow-md">@</div>
            <div class="flex-1 min-w-0">
                <p class="text-xs font-semibold text-indigo-400 leading-none">${title}</p>
                <p class="text-sm font-bold truncate mt-0.5">${subtitle}</p>
                <p class="text-xs text-zinc-300 mt-1 italic break-words">&ldquo;${snippet}&rdquo;</p>
                <a href="${url}" class="text-xs text-indigo-400 font-semibold hover:underline mt-2 inline-block">View &rarr;</a>
            </div>
            <button onclick="dismissToast('${toastId}')" class="text-zinc-500 hover:text-white p-1 rounded-full transition">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
            </button>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);
    const el = document.getElementById(toastId);
    requestAnimationFrame(() => {
        el.classList.remove('translate-x-full', 'opacity-0');
        el.classList.add('translate-x-0', 'opacity-100');
    });

    setTimeout(() => dismissToast(toastId), 6000);
}

function dismissToast(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove('translate-x-0', 'opacity-100');
    el.classList.add('translate-x-full', 'opacity-0');
    setTimeout(() => el.remove(), 300);
}

// WebSocket STOMP Initializer
let stompClient = null;

function initWebSockets() {
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') return;

    // Use SockJS options preventing obsolete unload hooks where possible
    const socket = new SockJS('/ws-feed');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
        
        // 1. Private User Notifications Queue (Mentions, Replies, Likes)
        stompClient.subscribe('/user/queue/notifications', (message) => {
            const notif = JSON.parse(message.body);
            const badge = document.getElementById('notification-badge');
            if (badge) {
                badge.textContent = notif.unreadCount > 99 ? '99+' : notif.unreadCount;
                badge.classList.remove('hidden');
            }
            showToast(
                'New Mention', 
                `${notif.actorDisplayName} @${notif.actorUsername}`, 
                notif.snippet, 
                `/tweets/${notif.targetTweetId}/thread`
            );
        });

        // 2. 1-on-1 & Group Direct Messages Queue
        stompClient.subscribe('/user/queue/messages', (frame) => {
            const msg = JSON.parse(frame.body);
            const activeConvoIdInput = document.getElementById('active-chat-convo-id');

            if (activeConvoIdInput && activeConvoIdInput.value === msg.conversationId) {
                const bubblesWrapper = document.getElementById('chat-bubbles-wrapper');
                if (bubblesWrapper) {
                    const senderHeader = (!msg.isSelf && msg.isGroup)
                        ? `<span class="text-[11px] font-semibold text-zinc-400 mb-1 px-1 flex items-center space-x-1">
                             <span>${msg.senderDisplayName}</span>
                             <span class="text-zinc-500 font-normal">@${msg.senderUsername}</span>
                           </span>`
                        : '';

                    let mediaHtml = '';
                    if (msg.mediaType === 'IMAGE') {
                        mediaHtml = `
                            <div class="overflow-hidden cursor-pointer">
                                <a href="${msg.mediaUrl}" target="_blank">
                                    <img src="${msg.mediaUrl}" alt="Attachment" class="max-h-60 w-auto rounded-t-xl object-cover hover:opacity-95 transition" />
                                </a>
                            </div>
                        `;
                    } else if (msg.mediaType === 'FILE') {
                        mediaHtml = `
                            <div class="p-3">
                                <a href="${msg.mediaUrl}" target="_blank" download class="flex items-center space-x-2.5 p-2 rounded-xl bg-zinc-800/80 hover:bg-zinc-800 text-zinc-200 transition">
                                    <svg class="w-6 h-6 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                    </svg>
                                    <div class="min-w-0 flex-1">
                                        <p class="font-semibold text-xs truncate">${msg.originalFileName}</p>
                                        <span class="text-[10px] opacity-75">Click to download</span>
                                    </div>
                                </a>
                            </div>
                        `;
                    }

                    const textHtml = msg.content ? `<p class="px-3.5 py-2.5">${msg.content}</p>` : '';

                    const bubbleHtml = `
                        <div id="msg-${msg.id}" class="flex flex-col mb-3 animate-fadeIn items-start">
                            ${senderHeader}
                            <div class="max-w-[75%] rounded-2xl text-xs break-words leading-relaxed shadow-sm overflow-hidden bg-zinc-900 text-zinc-100 rounded-bl-none border border-zinc-800">
                                ${mediaHtml}
                                ${textHtml}
                            </div>
                            <span class="text-[10px] text-zinc-500 mt-1 px-1">${msg.createdAtFormatted}</span>
                        </div>
                    `;
                    bubblesWrapper.insertAdjacentHTML('beforeend', bubbleHtml);
                    window.scrollMessagesToBottom();
                }
            } else {
                const title = msg.isGroup ? `Group: ${msg.conversationTitle}` : 'New Message';
                const previewText = msg.content || (msg.mediaType === 'IMAGE' ? '📷 Photo' : '📎 File attachment');
                showToast(
                    title, 
                    `${msg.senderDisplayName} @${msg.senderUsername}`, 
                    previewText, 
                    `/messages?convo=${msg.conversationId}`
                );
            }
        });

        // 3. Background WebP Media Optimization Broadcast
        stompClient.subscribe('/topic/tweet-media', (message) => {
            const data = JSON.parse(message.body);
            if (data.status === 'COMPLETED') {
                const mediaContainer = document.getElementById(`tweet-media-${data.tweetId}`);
                if (mediaContainer) {
                    mediaContainer.outerHTML = `
                        <div id="tweet-media-${data.tweetId}" class="mt-3 overflow-hidden rounded-2xl border border-zinc-800 relative aspect-video bg-zinc-900">
                            <div class="absolute inset-0 bg-cover bg-center filter blur-md scale-105"
                                 style="background-image: url('${data.blurDataUrl}');"></div>
                            <img src="${data.imageUrl}" 
                                 class="relative w-full h-full object-cover transition-opacity duration-300 opacity-0"
                                 onload="this.classList.remove('opacity-0');" />
                        </div>
                    `;
                }
            }
        });
    });
}

// Clean disconnection on modern page lifecycle events (avoiding deprecated unload)
window.addEventListener('pagehide', () => {
    if (stompClient !== null && stompClient.connected) {
        stompClient.disconnect();
    }
});