// 全局变量
let selectedFiles = [];
let conversionHistory = JSON.parse(localStorage.getItem('conversionHistory') || '[]');
let currentConversionMode = 'forward'; // 'forward' 或 'reverse'
let currentTargetFileType = 'gil'; // 反向转换时的目标文件类型
let currentOutputFormat = 'json1'; // 正向转换时的输出格式

const BASE_URL = 'http://localhost:1696'; // 后端地址

// DOM元素引用
const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const browseBtn = document.getElementById('browseBtn');
const fileList = document.getElementById('fileList');
const fileListContainer = document.getElementById('fileListContainer');
const clearBtn = document.getElementById('clearBtn');
const convertBtn = document.getElementById('convertBtn');
const progressContainer = document.getElementById('progressContainer');
const progressFill = document.getElementById('progressFill');
const progressText = document.getElementById('progressText');
const resultContainer = document.getElementById('resultContainer');
const resultContent = document.getElementById('resultContent');
const downloadBtn = document.getElementById('downloadBtn');
const historyList = document.getElementById('historyList');
const navButtons = document.querySelectorAll('.nav-btn');
const conversionModeInputs = document.querySelectorAll('input[name="conversionMode"]');
const outputFormatInputs = document.querySelectorAll('input[name="outputFormat"]');
const targetTypeInputs = document.querySelectorAll('input[name="targetFileType"]');
const forwardFormatSelection = document.getElementById('forwardFormatSelection');
const reverseTypeSelection = document.getElementById('reverseTypeSelection');
const supportedFormats = document.getElementById('supportedFormats');
const conversionInfo = document.getElementById('conversionInfo');
const directionInfo = document.getElementById('directionInfo');
const outputFormatInfo = document.getElementById('outputFormatInfo');
const targetTypeInfo = document.getElementById('targetTypeInfo');
const targetTypeInfoValue = document.getElementById('targetTypeInfoValue');

// 初始化应用
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupEventListeners();
    setupNavigation();
    loadConversionHistory();
    updateTabVisibility();
}

// 设置事件监听器
function setupEventListeners() {
    // 文件选择相关
    browseBtn.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileSelect);
    
    // 拖拽功能
    setupDragAndDrop();
    
    // 转换模式选择
    conversionModeInputs.forEach(input => {
        input.addEventListener('change', updateConversionMode);
    });
    
    // 输出格式选择
    outputFormatInputs.forEach(input => {
        input.addEventListener('change', updateOutputFormat);
    });
    
    // 目标文件类型选择
    targetTypeInputs.forEach(input => {
        input.addEventListener('change', updateTargetFileType);
    });
    
    // 按钮事件
    clearBtn.addEventListener('click', clearFileList);
    convertBtn.addEventListener('click', startConversion);
    downloadBtn.addEventListener('click', downloadResults);
    
    // 键盘快捷键
    document.addEventListener('keydown', handleKeyboardShortcuts);
}

// 设置导航功能
function setupNavigation() {
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            switchTab(tab);
        });
    });
}

// 切换标签页
function switchTab(tabName) {
    // 更新导航按钮状态
    navButtons.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });
    
    // 显示对应的内容
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === `${tabName}-tab`);
    });
    
    // 特殊处理
    if (tabName === 'history') {
        loadConversionHistory();
    }
}

// 更新标签页可见性
function updateTabVisibility() {
    // 这个函数可以用来动态控制标签页的显示/隐藏
    // 目前保持空实现，可以根据需要添加逻辑
}

// 设置拖拽和放下功能
function setupDragAndDrop() {
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefaults, false);
    });

    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, highlight, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, unhighlight, false);
    });

    dropZone.addEventListener('drop', handleDrop, false);
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}

function highlight() {
    dropZone.classList.add('drag-over');
}

function unhighlight() {
    dropZone.classList.remove('drag-over');
}

function handleDrop(e) {
    const dt = e.dataTransfer;
    const files = dt.files;
    handleFiles(files);
}

function handleFileSelect(e) {
    const files = e.target.files;
    handleFiles(files);
}

// 处理选中的文件
function handleFiles(files) {
    Array.from(files).forEach(file => {
        if (isValidFileType(file)) {
            addFileToList(file);
        } else {
            showMessage(`不支持的文件类型: ${file.name}`, 'error');
        }
    });
    
    updateUIState();
}

// 验证文件类型
function isValidFileType(file) {
    const validExtensions = currentConversionMode === 'forward' 
        ? ['.gil', '.gia', '.gip', '.gir']
        : ['.json', '.bin', '.pb'];
    const extension = '.' + file.name.split('.').pop().toLowerCase();
    return validExtensions.includes(extension);
}

// 添加文件到列表
function addFileToList(file) {
    // 检查是否已存在
    if (selectedFiles.some(f => f.name === file.name && f.size === file.size)) {
        showMessage(`${file.name} 已经在列表中`, 'warning');
        return;
    }
    
    selectedFiles.push(file);
    renderFileList();
    updateConversionInfo();
}

// 渲染文件列表
function renderFileList() {
    fileList.innerHTML = '';
    
    selectedFiles.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item slide-in-up';
        fileItem.style.animationDelay = `${index * 0.1}s`;
        
        fileItem.innerHTML = `
            <div class="file-info">
                <div class="file-icon">📄</div>
                <div>
                    <div class="file-name">${file.name}</div>
                    <div class="file-size">${formatFileSize(file.size)}</div>
                </div>
            </div>
            <button class="remove-btn" onclick="removeFile(${index})">×</button>
        `;
        
        fileList.appendChild(fileItem);
    });
}

// 移除文件
function removeFile(index) {
    selectedFiles.splice(index, 1);
    renderFileList();
    updateUIState();
}

// 清空文件列表
function clearFileList() {
    selectedFiles = [];
    renderFileList();
    updateUIState();
    fileInput.value = '';
    conversionInfo.style.display = 'none';
}

// 更新UI状态
function updateUIState() {
    const hasFiles = selectedFiles.length > 0;
    fileListContainer.style.display = hasFiles ? 'block' : 'none';
    convertBtn.disabled = !hasFiles;
    
    // 如果没有文件，隐藏进度和结果
    if (!hasFiles) {
        progressContainer.style.display = 'none';
        resultContainer.style.display = 'none';
    }
}

// 更新转换模式
function updateConversionMode() {
    currentConversionMode = document.querySelector('input[name="conversionMode"]:checked').value;
    
    // 更新文件输入的accept属性
    fileInput.accept = currentConversionMode === 'forward' 
        ? '.gil,.gia,.gip,.gir' 
        : '.json,.bin,.pb';
    
    // 更新支持的格式文本
    supportedFormats.textContent = currentConversionMode === 'forward'
        ? '支持 .gil, .gia, .gip, .gir 格式'
        : '支持 .json, .bin(pb), .pb 格式';
    
    // 控制格式选择区域的显示
    forwardFormatSelection.style.display = currentConversionMode === 'forward' ? 'block' : 'none';
    reverseTypeSelection.style.display = currentConversionMode === 'reverse' ? 'block' : 'none';
    
    // 更新拖拽区域样式
    dropZone.classList.remove('forward-mode', 'reverse-mode');
    dropZone.classList.add(currentConversionMode + '-mode');
    
    // 清空当前文件列表
    clearFileList();
    
    // 更新转换信息显示
    updateConversionInfo();
}

// 更新输出格式
function updateOutputFormat() {
    currentOutputFormat = document.querySelector('input[name="outputFormat"]:checked').value;
    updateConversionInfo();
}

// 更新目标文件类型
function updateTargetFileType() {
    currentTargetFileType = document.querySelector('input[name="targetFileType"]:checked').value;
    updateConversionInfo();
}

// 更新转换信息显示
function updateConversionInfo() {
    if (selectedFiles.length > 0) {
        conversionInfo.style.display = 'flex';
        directionInfo.textContent = currentConversionMode === 'forward' ? '原始 → 输出格式' : 'JSON → 原始';
        
        // 正向转换时显示输出格式
        if (currentConversionMode === 'forward') {
            const formatNames = {
                'json1': 'JSON格式 - Name Key',
                'json2': 'SON格式 - Field Key',
                'pb': 'Protocol Buffer'
            };
            outputFormatInfo.textContent = formatNames[currentOutputFormat] || currentOutputFormat;
            targetTypeInfo.style.display = 'none';
        } else {
            // 反向转换时显示目标文件类型
            outputFormatInfo.textContent = '原始二进制格式';
            targetTypeInfo.style.display = 'flex';
            targetTypeInfoValue.textContent = `.${currentTargetFileType} 文件`;
        }
    } else {
        conversionInfo.style.display = 'none';
    }
}

// 开始转换 - 精简版本
async function startConversion() {
    if (selectedFiles.length === 0) return;
    
    progressContainer.style.display = 'block';
    resultContainer.style.display = 'none';
    convertBtn.disabled = true;
    
    try {
        const results = [];
        const totalFiles = selectedFiles.length;
        
        // 执行转换
        for (let i = 0; i < selectedFiles.length; i++) {
            const file = selectedFiles[i];
            const progress = ((i + 1) / totalFiles) * 100;
            updateProgress(progress, `正在转换 ${file.name}`);
            
            const result = await convertFile(file);
            results.push(result);
            await delay(300);
        }
        
        // 完成处理
        updateProgress(100, '转换完成！');
        showResults(results);
        saveToHistory(selectedFiles);
        showMessage('✅ 转换成功！请记得下载保存结果文件', 'success');
        
    } catch (error) {
        console.error('转换失败:', error);
        showMessage('❌ 转换失败，请重试', 'error');
        updateProgress(0, '转换失败');
    } finally {
        convertBtn.disabled = false;
    }
}

// 转换单个文件
async function convertFile(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        
        reader.onload = async function(e) {
            try {
                const arrayBuffer = e.target.result;
                
                // 将二进制数据转换为base64 - 使用安全的方式
                const base64Data = arrayBufferToBase64(arrayBuffer);
                
                // 准备发送到后端的数据
                const requestData = {
                    fileName: file.name,
                    fileType: file.name.split('.').pop().toLowerCase(),
                    base64Data: base64Data
                };
                
                let response;
                
                if (currentConversionMode === 'forward') {
                    // 正向转换：调用 /api/conversion/forward
                    const queryParams = new URLSearchParams({
                        outputFormat: currentOutputFormat
                    });
                    
                    response = await fetch(BASE_URL + `/api/conversion/forward?${queryParams.toString()}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify(requestData)
                    });
                    
                } else {
                    // 反向转换：调用 /api/conversion/reverse
                    const queryParams = new URLSearchParams({
                        targetFileType: currentTargetFileType
                    });
                    
                    response = await fetch(BASE_URL+`/api/conversion/reverse?${queryParams.toString()}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify(requestData)
                    });
                }
                
                if (!response.ok) {
                    const errorMessage = await response.text();
                    throw new Error(`HTTP error! status: ${response.status}, message: ${errorMessage}`);
                }
                
                // 根据转换模式处理响应
                if (currentConversionMode === 'forward') {
                    // 正向转换直接返回数据
                    const result = await response.text();
                    resolve(result);
                } else {
                    // 反向转换返回base64数据
                    const base64Result = await response.text();
                    resolve(base64Result);
                }
                
            } catch (error) {
                reject(error);
            }
        };
        
        reader.onerror = () => reject(new Error('文件读取失败'));
        reader.readAsArrayBuffer(file);
    });
}

// 更新进度显示
function updateProgress(percentage, text) {
    progressFill.style.width = `${percentage}%`;
    progressText.textContent = text;
}

// 显示结果
function showResults(results) {
    resultContainer.style.display = 'block';
    resultContent.innerHTML = '';
    
    results.forEach(result => {
        const resultElement = document.createElement('div');
        resultElement.className = 'success-message';
        
        if (currentConversionMode === 'forward') {
            const resultText = typeof result === 'string' ? result : JSON.stringify(result, null, 2);
            resultElement.innerHTML = `
                <div class="result-header">
                    <button class="copy-btn" onclick="copyResultToClipboard(this, decodeURIComponent('${encodeURIComponent(resultText)}'))">
                        📋 复制结果
                    </button>
                </div>
                <pre>${resultText}</pre>
            `;
        } else {
            resultElement.innerHTML = `
                <div class="binary-preview">
                    <p>二进制数据已生成</p>
                </div>
            `;
        }
        
        resultContent.appendChild(resultElement);
    });
    
    // 保存结果供下载
    window.conversionResults = results;
}

// 下载结果
function downloadResults() {
    if (!window.conversionResults) return;
    
    if (currentConversionMode === 'forward') {
        // 正向转换：下载格式化结果
        downloadFormattedResults();
    } else {
        // 反向转换：下载二进制文件
        downloadBinaryResults();
    }
}

// 下载格式化结果 - 使用历史记录中生成的文件名
function downloadFormattedResults() {
    if (!window.conversionResults) return;
    
    window.conversionResults.forEach((result, index) => {
        // 使用历史记录中生成的文件名
        const originalFile = selectedFiles[index];
        const convertedFileName = generateConvertedFileName(
            originalFile?.name, 
            currentConversionMode, 
            currentOutputFormat
        );
        
        let content = result;
        let mimeType = 'application/octet-stream';
        
        // 根据格式设置MIME类型
        if (currentOutputFormat === 'json1' || currentOutputFormat === 'json2') {
            mimeType = 'application/json';
            if (typeof content === 'object') {
                content = JSON.stringify(content, null, currentOutputFormat === 'json2' ? 2 : 0);
            }
        } else if (currentOutputFormat === 'pb') {
            mimeType = 'application/octet-stream';
            if (typeof content === 'string') {
                try {
                    const binaryString = atob(content);
                    const bytes = new Uint8Array(binaryString.length);
                    for (let i = 0; i < binaryString.length; i++) {
                        bytes[i] = binaryString.charCodeAt(i);
                    }
                    content = bytes;
                } catch (e) {
                    console.error('PB数据解码失败:', e);
                }
            }
        }
        
        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = convertedFileName; // 使用正确的文件名
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    });
}

// 下载二进制结果 - 使用正确的文件名
function downloadBinaryResults() {
    if (!window.conversionResults) return;
    
    window.conversionResults.forEach((result, index) => {
        if (result && typeof result === 'string') {
            try {
                // 解码base64数据
                const binaryString = atob(result);
                const bytes = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }
                
                const blob = new Blob([bytes], { type: 'application/octet-stream' });
                const url = URL.createObjectURL(blob);
                
                // 使用历史记录中生成的文件名
                const originalFile = selectedFiles[index];
                const convertedFileName = generateConvertedFileName(
                    originalFile?.name, 
                    currentConversionMode, 
                    currentTargetFileType
                );
                
                const a = document.createElement('a');
                a.href = url;
                a.download = convertedFileName;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                URL.revokeObjectURL(url);
                
            } catch (error) {
                console.error('解码base64数据失败:', error);
                showMessage('下载失败：数据解码错误', 'error');
            }
        }
    });
}

// 下载单个二进制结果
// 保存到历史记录 - 支持多文件转换
function saveToHistory(files) {
    try {
        const historyItems = [];
        
        // 为每个文件创建历史记录项
        files.forEach((file, index) => {
            const historyItem = {
                id: Date.now() + index, // 确保每个项目有唯一ID
                originalFileName: file.name,
                fileCount: 1, // 每个记录只对应一个文件
                timestamp: new Date().toISOString(),
                mode: currentConversionMode,
                format: currentConversionMode === 'forward' ? currentOutputFormat : currentTargetFileType,
                convertedFileName: generateConvertedFileName(
                    file.name, 
                    currentConversionMode, 
                    currentConversionMode === 'forward' ? currentOutputFormat : currentTargetFileType
                )
            };
            historyItems.push(historyItem);
        });
        
        let history = [];
        
        // 安全读取现有历史
        try {
            const stored = localStorage.getItem('conversionHistory');
            if (stored) {
                history = JSON.parse(stored);
            }
        } catch (e) {
            console.warn('读取历史记录失败');
        }
        
        // 将新记录添加到历史中（限制总数）
        history.unshift(...historyItems);
        
        // 限制总数量（最多50条，避免过多）
        if (history.length > 30) {
            history = history.slice(0, 30);
        }
        
        // 保存到存储
        localStorage.setItem('conversionHistory', JSON.stringify(history));
        conversionHistory = history;
        
    } catch (error) {
        console.warn('保存历史记录失败:', error);
    }
}

// 生成转换后的文件名 - 移除时间戳，保持简洁
function generateConvertedFileName(originalName, mode, format) {
    if (!originalName) return 'nlo.dat';
    
    // 提取原始文件名（不含扩展名）
    const lastDotIndex = originalName.lastIndexOf('.');
    const nameWithoutExt = lastDotIndex > 0 ? originalName.substring(0, lastDotIndex) : originalName;
    const extension = mode === 'forward' ? 
        (format === 'pb' ? 'bin' : 'json') : 
        format;
    
    // 确保文件名安全，移除特殊字符
    const safeName = nameWithoutExt.replace(/[<>:"/\\|?*\x00-\x1F]/g, '_');
    
    // 检查是否已经有 nailong_output_ 前缀
    const hasPrefix = safeName.startsWith('nlo_');
    const baseName = hasPrefix ? safeName : `nlo_${safeName}`;

    const extension2 = originalName.split('.').pop();

    console.log(extension2)
    const addTypeName =
        (extension2 === "gia" || extension2 === "gil" || extension2 === "gir" || extension2 === "gip") ? ("_" + extension2) : "";
    
    return `${baseName}${addTypeName}.${extension}`;
}

// 加载转换历史 - 支持单文件记录显示
function loadConversionHistory() {
    historyList.innerHTML = '';
    
    let history = [];
    try {
        const stored = localStorage.getItem('conversionHistory');
        if (stored) {
            history = JSON.parse(stored);
            conversionHistory = history;
        }
    } catch (error) {
        console.warn('读取历史记录失败');
        conversionHistory = [];
    }
    
    if (history.length === 0) {
        historyList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📋</div>
                <h3 style="font-size: 20px; font-weight: 600; margin-bottom: 10px;">暂无转换记录</h3>
                <p style="font-size: 16px; color: #888;">
                    完成文件转换后，历史记录将显示在这里
                </p>
            </div>
        `;
        return;
    }
    
    // 显示历史记录（每个文件一条记录）
    history.forEach(item => {
        const historyItem = document.createElement('div');
        historyItem.className = 'history-item';
        
        const date = new Date(item.timestamp).toLocaleString('zh-CN');
        const modeIcon = item.mode === 'forward' ? '📤' : '📥';
        const modeText = item.mode === 'forward' ? '正向转换' : '反向转换';
        
        historyItem.innerHTML = `
            <div class="history-info">
                <div style="margin-bottom: 15px;">
                    <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                        <div style="font-size: 20px;">${modeIcon}</div>
                        <h3 style="font-size: 18px; font-weight: 600; margin: 0; color: #333;">
                            ${item.originalFileName}
                        </h3>
                    </div>
                    <div style="margin-left: 32px;">
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
                            <span style="font-size: 14px; color: #007AFF;">⬇️ 转换结果:</span>
                            <span style="font-size: 15px; font-weight: 500; color: #333; background: #f0f8ff; padding: 4px 10px; border-radius: 6px;">
                                ${item.convertedFileName}
                            </span>
                        </div>
                        <div style="font-size: 14px; color: #666;">
                            📅 ${date}
                        </div>
                    </div>
                </div>
                
                <div style="display: flex; align-items: center; gap: 20px; font-size: 14px; color: #666; margin-left: 32px; margin-top: 10px;">
                    <span>${modeText}</span>
                    <span style="text-transform: uppercase; background: #e8f4fd; padding: 2px 8px; border-radius: 4px;">
                        ${item.format}
                    </span>
                </div>
            </div>
            <div class="history-actions">
                <button class="btn btn-secondary" onclick="showHistoryDetails(${item.id})" style="padding: 10px 20px; font-size: 15px;">
                    查看详情
                </button>
            </div>
        `;
        
        historyList.appendChild(historyItem);
    });
}

// 显示历史记录详情
function showHistoryDetails(id) {
    const item = conversionHistory.find(h => h.id === id);
    if (!item) return;
    
    // 创建模态框
    const modal = document.createElement('div');
    modal.id = 'history-modal-' + id;
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
        backdrop-filter: blur(5px);
    `;
    
    const modalContent = document.createElement('div');
    modalContent.style.cssText = `
        background: white;
        border-radius: 16px;
        padding: 30px;
        width: 90%;
        max-width: 500px;
        max-height: 80vh;
        overflow-y: auto;
        box-shadow: 0 20px 40px rgba(0,0,0,0.2);
        transform: scale(0.9);
        transition: transform 0.3s ease;
    `;
    
    const date = new Date(item.timestamp).toLocaleString('zh-CN');
    const modeText = item.mode === 'forward' ? '正向转换' : '反向转换';
    
    // 创建关闭函数
    const closeFunction = function() {
        modal.remove();
    };
    
    modalContent.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
            <h2 style="margin: 0; font-size: 24px; font-weight: 600; color: #333;">转换详情</h2>
            <button id="modal-close-btn" 
                    style="background: none; border: none; font-size: 24px; cursor: pointer; color: #999; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;">×</button>
        </div>
        
        <div style="display: grid; gap: 20px;">
            <div style="background: #f8f9fa; padding: 20px; border-radius: 12px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <h3 style="margin: 0; font-size: 18px; color: #333;">📁 文件信息</h3>
                    ${item.mode === 'forward' ? `<button class="btn btn-secondary" onclick="copyHistoryResult(${item.id})" style="padding: 6px 12px; font-size: 13px;">
                        📋 复制结果
                    </button>` : ''}
                </div>
                <div style="display: grid; gap: 12px; font-size: 15px;">
                    <div><strong>原始文件:</strong> ${item.originalFileName}</div>
                    <div><strong>转换结果:</strong> 
                        <span style="color: #007AFF; font-weight: 500;">${item.convertedFileName}</span>
                    </div>
                </div>
            </div>
            
            <div style="background: #f8f9fa; padding: 20px; border-radius: 12px;">
                <h3 style="margin: 0 0 15px 0; font-size: 18px; color: #333;">⚙️ 转换参数</h3>
                <div style="display: grid; gap: 12px; font-size: 15px;">
                    <div><strong>转换模式:</strong> ${modeText}</div>
                    <div><strong>目标格式:</strong> 
                        <span style="text-transform: uppercase; background: #e8f4fd; padding: 2px 8px; border-radius: 4px;">
                            ${item.format}
                        </span>
                    </div>
                    <div><strong>转换时间:</strong> ${date}</div>
                </div>
            </div>
            
            <div style="background: #e3f2fd; padding: 20px; border-radius: 12px; border-left: 4px solid #2196F3;">
                <h3 style="margin: 0 0 15px 0; font-size: 18px; color: #0d47a1;">💡 使用提示</h3>
                <p style="margin: 0; font-size: 15px; line-height: 1.6; color: #333;">
                    转换结果文件需要您手动下载保存。如需重新执行相同的转换，
                    可以参考以上设置信息重新操作。
                </p>
            </div>
        </div>
    `;
    
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
    
    // 显示动画
    setTimeout(() => {
        modalContent.style.transform = 'scale(1)';
    }, 50);
    
    // 绑定关闭事件
    const closeBtn = modalContent.querySelector('#modal-close-btn');
    if (closeBtn) {
        closeBtn.addEventListener('click', closeFunction);
    }
    
    // 点击背景关闭
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeFunction();
        }
    });
    
    // ESC键关闭
    const escHandler = function(e) {
        if (e.key === 'Escape') {
            closeFunction();
            document.removeEventListener('keydown', escHandler);
        }
    };
    document.addEventListener('keydown', escHandler);
}

// 工具函数
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// 辅助函数：安全地将ArrayBuffer转换为base64
function arrayBufferToBase64(buffer) {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const chunkSize = 8192; // 分块处理避免内存问题
    
    // 分块处理大文件
    for (let i = 0; i < bytes.length; i += chunkSize) {
        const chunk = bytes.subarray(i, i + chunkSize);
        binary += String.fromCharCode.apply(null, chunk);
    }
    
    return btoa(binary);
}

// 优化的消息显示函数
function showMessage(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    
    Object.assign(toast.style, {
        position: 'fixed',
        top: '20px',
        right: '20px',
        padding: '14px 24px',
        borderRadius: '10px',
        color: 'white',
        fontWeight: '500',
        fontSize: '15px',
        zIndex: '1000',
        boxShadow: '0 6px 20px rgba(0,0,0,0.2)',
        transform: 'translateX(100%)',
        transition: 'transform 0.3s ease, opacity 0.3s ease',
        opacity: '0'
    });
    
    const colors = {
        success: '#34C759',
        error: '#FF3B30',
        warning: '#FF9500',
        info: '#007AFF'
    };
    toast.style.backgroundColor = colors[type] || colors.info;
    
    document.body.appendChild(toast);
    
    // 显示动画
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
        toast.style.opacity = '1';
    }, 100);
    
    // 自动消失
    setTimeout(() => {
        toast.style.transform = 'translateX(100%)';
        toast.style.opacity = '0';
        setTimeout(() => {
            if (toast.parentNode) {
                document.body.removeChild(toast);
            }
        }, 300);
    }, 4000);
}

// 键盘快捷键
function handleKeyboardShortcuts(e) {
    // Ctrl+B 浏览文件
    if (e.ctrlKey && e.key === 'b') {
        e.preventDefault();
        browseBtn.click();
    }
    
    // Ctrl+Enter 开始转换
    if (e.ctrlKey && e.key === 'Enter' && selectedFiles.length > 0) {
        e.preventDefault();
        convertBtn.click();
    }
    
    // ESC 清空列表
    if (e.key === 'Escape') {
        clearFileList();
    }
}

// 复制历史记录结果
function copyHistoryResult(id) {
    const item = conversionHistory.find(h => h.id === id);
    if (!item || item.mode !== 'forward') {
        showMessage('❌ 无法复制此记录的结果', 'error');
        return;
    }
    
    // 简化处理：复制转换后的文件名作为示例结果
    const resultText = `{
  "message": "这是来自历史记录 ${item.originalFileName} 的转换结果",
  "convertedFileName": "${item.convertedFileName}",
  "timestamp": "${new Date().toISOString()}"
}`;
    
    copyTextToClipboard(resultText);
}

// 复制结果到剪贴板
function copyResultToClipboard(button, resultText) {
    try {
        // 显示加载状态
        const originalText = button.innerHTML;
        button.innerHTML = '⏳ 复制中...';
        button.disabled = true;
        
        // 使用通用复制函数
        copyTextToClipboard(resultText);
        
        // 恢复按钮状态
        setTimeout(() => {
            button.innerHTML = '✅ 已复制';
            button.style.backgroundColor = '#4CAF50';
            button.disabled = false;
            
            setTimeout(() => {
                button.innerHTML = originalText;
                button.style.backgroundColor = '';
            }, 2000);
        }, 500);
        
    } catch (err) {
        console.error('复制按钮处理失败:', err);
        showMessage('❌ 复制功能出现错误', 'error');
    }
}

// 通用文本复制函数
function copyTextToClipboard(text) {
    try {
        // 现代浏览器API
        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(text).then(() => {
                showMessage('📋 结果已复制到剪贴板 - 通用模式', 'success');
            }).catch(err => {
                console.error('现代API复制失败:', err);
                fallbackCopyTextToClipboard(text);
            });
        } else {
            // 降级方案
            fallbackCopyTextToClipboard(text);
        }
    } catch (err) {
        console.error('复制失败:', err);
        showMessage('❌ 复制失败，请手动选择文本复制', 'error');
    }
}

// 降级复制方案
function fallbackCopyTextToClipboard(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    textarea.style.top = '-9999px';
    document.body.appendChild(textarea);
    
    textarea.select();
    textarea.setSelectionRange(0, 99999);
    
    try {
        const successful = document.execCommand('copy');
        document.body.removeChild(textarea);
        
        if (successful) {
            showMessage('📋 结果已复制到剪贴板 - 降级模式', 'success');
        } else {
            throw new Error('复制命令失败');
        }
    } catch (err) {
        console.error('降级复制失败:', err);
        document.body.removeChild(textarea);
        showMessage('❌ 复制失败，请手动选择文本复制', 'error');
    }
}

// 页面可见性变化时更新历史记录
document.addEventListener('visibilitychange', function() {
    if (!document.hidden) {
        loadConversionHistory();
    }
});