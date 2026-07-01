package myau.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import myau.Myau;
import myau.module.Module;
import myau.module.modules.*;
import myau.property.Property;
import myau.property.properties.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class WebUIManager {
    private static HttpServer server;
    private static boolean running = false;
    private static final int PORT = 8080;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String INDEX_HTML =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Myau WebUI</title>\n" +
                    "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css\">\n" +
                    "    <style>\n" +
                    "        * { margin:0; padding:0; box-sizing:border-box; font-family: 'Segoe UI', system-ui, sans-serif; }\n" +
                    "        :root {\n" +
                    "            --bg: #f5f7fa;\n" +
                    "            --sidebar-bg: rgba(255,255,255,0.6);\n" +
                    "            --card-bg: rgba(255,255,255,0.5);\n" +
                    "            --card-hover: rgba(255,255,255,0.7);\n" +
                    "            --text-primary: #2c3e50;\n" +
                    "            --text-secondary: #3d4a5a;\n" +
                    "            --border-light: rgba(255,255,255,0.3);\n" +
                    "            --shadow: 0 4px 16px rgba(0,0,0,0.03);\n" +
                    "            --toggle-off: #cbd5e1;\n" +
                    "            --toggle-on: #2ecc71;\n" +
                    "            --scrollbar: #cbd5e1;\n" +
                    "            --scrollbar-hover: #94a3b8;\n" +
                    "            --mode-bg: rgba(0,0,0,0.04);\n" +
                    "            --mode-active: #3498db;\n" +
                    "        }\n" +
                    "        body.dark {\n" +
                    "            --bg: #1a1a1a;\n" +
                    "            --sidebar-bg: rgba(30,30,30,0.8);\n" +
                    "            --card-bg: rgba(40,40,40,0.6);\n" +
                    "            --card-hover: rgba(50,50,50,0.8);\n" +
                    "            --text-primary: #e0e0e0;\n" +
                    "            --text-secondary: #b0b0b0;\n" +
                    "            --border-light: rgba(255,255,255,0.08);\n" +
                    "            --shadow: 0 4px 16px rgba(0,0,0,0.3);\n" +
                    "            --toggle-off: #4a5568;\n" +
                    "            --toggle-on: #2ecc71;\n" +
                    "            --scrollbar: #4a5568;\n" +
                    "            --scrollbar-hover: #718096;\n" +
                    "            --mode-bg: rgba(255,255,255,0.06);\n" +
                    "            --mode-active: #3498db;\n" +
                    "        }\n" +
                    "        body { background: var(--bg); min-height:100vh; transition: background 0.4s ease; }\n" +
                    "        #app { display: flex; height: 100vh; backdrop-filter: blur(10px); background: rgba(255,255,255,0.3); transition: background 0.4s ease; }\n" +
                    "        body.dark #app { background: rgba(0,0,0,0.2); }\n" +
                    "        #sidebar { width: 220px; background: var(--sidebar-bg); backdrop-filter: blur(12px); box-shadow: 2px 0 12px rgba(0,0,0,0.05); border-right: 1px solid var(--border-light); border-radius: 0 20px 20px 0; padding: 24px 16px; overflow-y: auto; flex-shrink: 0; transition: background 0.4s, border 0.4s; }\n" +
                    "        .logo { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 30px; letter-spacing: 1px; transition: color 0.4s; }\n" +
                    "        #category-list { list-style: none; }\n" +
                    "        #category-list li { padding: 12px 16px; margin: 4px 0; border-radius: 12px; cursor: pointer; font-weight: 500; color: var(--text-primary); transition: all 0.2s ease; background: transparent; }\n" +
                    "        #category-list li:hover { background: rgba(0,0,0,0.04); transform: scale(1.02); }\n" +
                    "        body.dark #category-list li:hover { background: rgba(255,255,255,0.06); }\n" +
                    "        #category-list li.active { background: rgba(52,152,219,0.15); color: #2980b9; font-weight: 600; }\n" +
                    "        #content { flex:1; padding: 30px 40px; overflow-y: auto; }\n" +
                    "        #module-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; align-items: start; }\n" +
                    "        .module-card { background: var(--card-bg); backdrop-filter: blur(10px); border-radius: 16px; padding: 16px 20px; box-shadow: var(--shadow); border: 1px solid var(--border-light); transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94); cursor: default; display: flex; flex-direction: column; gap: 12px; min-height: 0; }\n" +
                    "        .module-card:hover { transform: translateY(-4px) scale(1.01); box-shadow: 0 8px 30px rgba(0,0,0,0.06); background: var(--card-hover); }\n" +
                    "        body.dark .module-card:hover { box-shadow: 0 8px 30px rgba(0,0,0,0.4); }\n" +
                    "        .card-header { display: flex; align-items: center; justify-content: space-between; }\n" +
                    "        .module-name { font-weight: 600; font-size: 16px; color: var(--text-primary); transition: color 0.4s; }\n" +
                    "        .module-controls { display: flex; align-items: center; gap: 10px; }\n" +
                    "        .bind-btn { background: none; border: 1px solid rgba(0,0,0,0.1); border-radius: 8px; width: 32px; height: 32px; font-size: 14px; color: var(--text-secondary); cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }\n" +
                    "        .bind-btn:hover { background: rgba(0,0,0,0.04); border-color: #3498db; color: #3498db; }\n" +
                    "        body.dark .bind-btn { border-color: rgba(255,255,255,0.15); color: #b0b0b0; }\n" +
                    "        body.dark .bind-btn:hover { background: rgba(255,255,255,0.06); border-color: #3498db; color: #3498db; }\n" +
                    "        .toggle-switch { width: 44px; height: 24px; background: var(--toggle-off); border-radius: 12px; cursor: pointer; transition: 0.3s; position: relative; flex-shrink: 0; }\n" +
                    "        .toggle-switch.active { background: var(--toggle-on); }\n" +
                    "        .toggle-switch::after { content: \"\"; position: absolute; top: 2px; left: 2px; width: 20px; height: 20px; background: white; border-radius: 50%; transition: 0.3s; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
                    "        .toggle-switch.active::after { left: 22px; }\n" +
                    "        .module-properties { max-height: 0; overflow: hidden; transition: max-height 0.35s ease, opacity 0.3s ease; opacity: 0; }\n" +
                    "        .module-properties.open { max-height: 250px; opacity: 1; overflow-y: auto; }\n" +
                    "        .module-properties.open::-webkit-scrollbar { width: 6px; }\n" +
                    "        .module-properties.open::-webkit-scrollbar-thumb { background: linear-gradient(var(--scrollbar), var(--scrollbar-hover)); border-radius: 6px; border: 1px solid var(--border-light); }\n" +
                    "        .module-properties.open::-webkit-scrollbar-track { background: transparent; }\n" +
                    "        .property-row { display: flex; align-items: center; justify-content: space-between; padding: 6px 0; border-top: 1px solid rgba(0,0,0,0.04); }\n" +
                    "        body.dark .property-row { border-top-color: rgba(255,255,255,0.06); }\n" +
                    "        .property-name { font-size: 14px; color: var(--text-secondary); transition: color 0.4s; }\n" +
                    "        .property-value { display: flex; align-items: center; gap: 8px; }\n" +
                    "        .property-toggle { width: 32px; height: 18px; border-radius: 9px; background: var(--toggle-off); cursor: pointer; transition: 0.2s; position: relative; }\n" +
                    "        .property-toggle.active { background: var(--toggle-on); }\n" +
                    "        .property-toggle::after { content: \"\"; position: absolute; top: 2px; left: 2px; width: 14px; height: 14px; background: white; border-radius: 50%; transition: 0.2s; }\n" +
                    "        .property-toggle.active::after { left: 16px; }\n" +
                    "        input[type=\"range\"] { width: 120px; height: 4px; background: var(--toggle-off); border-radius: 2px; -webkit-appearance: none; outline: none; transition: background 0.4s; }\n" +
                    "        body.dark input[type=\"range\"] { background: #4a5568; }\n" +
                    "        input[type=\"range\"]::-webkit-slider-thumb { -webkit-appearance: none; appearance: none; width: 14px; height: 14px; border-radius: 50%; background: #3498db; cursor: pointer; border: 2px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.1); }\n" +
                    "        input[type=\"range\"]::-moz-range-thumb { width: 14px; height: 14px; border-radius: 50%; background: #3498db; cursor: pointer; border: 2px solid white; }\n" +
                    "        .mode-selector { display: flex; gap: 4px; overflow-x: auto; overflow-y: hidden; white-space: nowrap; padding: 2px 4px; max-width: 160px; scrollbar-width: thin; scrollbar-color: #3498db rgba(0,0,0,0.05); }\n" +
                    "        .mode-selector::-webkit-scrollbar { height: 6px; background: transparent; }\n" +
                    "        .mode-selector::-webkit-scrollbar-track { background: rgba(0,0,0,0.05); border-radius: 4px; }\n" +
                    "        .mode-selector::-webkit-scrollbar-thumb { background: linear-gradient(135deg, #3498db, #2980b9); border-radius: 4px; box-shadow: 0 2px 6px rgba(52,152,219,0.3); transition: background 0.3s; }\n" +
                    "        .mode-selector::-webkit-scrollbar-thumb:hover { background: linear-gradient(135deg, #2980b9, #1f6e9b); }\n" +
                    "        .mode-option { padding: 2px 8px; border-radius: 6px; background: var(--mode-bg); font-size: 12px; cursor: pointer; border: 1px solid transparent; transition: 0.2s; color: var(--text-primary); flex-shrink: 0; }\n" +
                    "        .mode-option.active { background: var(--mode-active); color: white; border-color: #2980b9; }\n" +
                    "        .mode-option:hover { background: rgba(52,152,219,0.1); }\n" +
                    "        body.dark .mode-option:hover { background: rgba(52,152,219,0.2); }\n" +
                    "        input[type=\"color\"] { width: 30px; height: 30px; border: 1px solid #ccc; border-radius: 6px; cursor: pointer; background: none; padding: 0; }\n" +
                    "        body.dark input[type=\"color\"] { border-color: #555; }\n" +
                    "        #theme-toggle {\n" +
                    "            position: fixed;\n" +
                    "            bottom: 24px;\n" +
                    "            right: 24px;\n" +
                    "            width: 48px;\n" +
                    "            height: 48px;\n" +
                    "            border-radius: 50%;\n" +
                    "            background: var(--card-bg);\n" +
                    "            backdrop-filter: blur(8px);\n" +
                    "            border: 1px solid var(--border-light);\n" +
                    "            box-shadow: var(--shadow);\n" +
                    "            cursor: pointer;\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "            font-size: 24px;\n" +
                    "            color: var(--text-primary);\n" +
                    "            transition: background 0.4s, border 0.4s, color 0.4s, transform 0.3s ease;\n" +
                    "            z-index: 999;\n" +
                    "        }\n" +
                    "        #theme-toggle:hover { transform: scale(1.1); }\n" +
                    "        @keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(230,126,34,0.4); } 70% { box-shadow: 0 0 0 8px rgba(230,126,34,0); } 100% { box-shadow: 0 0 0 0 rgba(230,126,34,0); } }\n" +
                    "        ::-webkit-scrollbar { width: 8px; height: 8px; }\n" +
                    "        ::-webkit-scrollbar-track { background: transparent; }\n" +
                    "        ::-webkit-scrollbar-thumb { background: var(--scrollbar); border-radius: 8px; border: 1px solid var(--border-light); box-shadow: inset 0 0 0 1px rgba(255,255,255,0.05); transition: background 0.3s; }\n" +
                    "        ::-webkit-scrollbar-thumb:hover { background: var(--scrollbar-hover); }\n" +
                    "        ::-webkit-scrollbar-corner { background: transparent; }\n" +
                    "        /* Toast 通知 */\n" +
                    "        #toast-container {\n" +
                    "            position: fixed;\n" +
                    "            bottom: 80px;\n" +
                    "            right: 24px;\n" +
                    "            z-index: 1100;\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "            align-items: flex-end;\n" +
                    "            gap: 8px;\n" +
                    "        }\n" +
                    "        .toast {\n" +
                    "            background: var(--card-bg);\n" +
                    "            backdrop-filter: blur(12px);\n" +
                    "            border: 1px solid var(--border-light);\n" +
                    "            border-radius: 12px;\n" +
                    "            padding: 12px 20px;\n" +
                    "            box-shadow: 0 4px 16px rgba(0,0,0,0.15);\n" +
                    "            color: var(--text-primary);\n" +
                    "            font-size: 14px;\n" +
                    "            opacity: 0;\n" +
                    "            transform: translateX(20px);\n" +
                    "            animation: toastIn 0.3s ease forwards;\n" +
                    "            max-width: 300px;\n" +
                    "            transition: background 0.4s, border 0.4s, color 0.4s;\n" +
                    "        }\n" +
                    "        .toast.success { border-left: 4px solid #2ecc71; }\n" +
                    "        .toast.error { border-left: 4px solid #e74c3c; }\n" +
                    "        .toast.info { border-left: 4px solid #3498db; }\n" +
                    "        @keyframes toastIn {\n" +
                    "            from { opacity: 0; transform: translateX(20px); }\n" +
                    "            to { opacity: 1; transform: translateX(0); }\n" +
                    "        }\n" +
                    "        @keyframes toastOut {\n" +
                    "            from { opacity: 1; transform: translateX(0); }\n" +
                    "            to { opacity: 0; transform: translateX(20px); }\n" +
                    "        }\n" +
                    "\n" +
                    "        /* 自定义模态框 */\n" +
                    "        .modal-overlay {\n" +
                    "            position: fixed;\n" +
                    "            top: 0; left: 0; right: 0; bottom: 0;\n" +
                    "            background: rgba(0,0,0,0.5);\n" +
                    "            backdrop-filter: blur(4px);\n" +
                    "            display: none;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "            z-index: 1000;\n" +
                    "        }\n" +
                    "        .modal-overlay.active { display: flex; }\n" +
                    "        .modal-box {\n" +
                    "            background: var(--card-bg);\n" +
                    "            backdrop-filter: blur(10px);\n" +
                    "            border-radius: 16px;\n" +
                    "            padding: 30px;\n" +
                    "            border: 1px solid var(--border-light);\n" +
                    "            box-shadow: var(--shadow);\n" +
                    "            max-width: 400px;\n" +
                    "            width: 90%;\n" +
                    "            transition: background 0.4s, border 0.4s;\n" +
                    "        }\n" +
                    "        .modal-box h3 { color: var(--text-primary); margin-bottom: 16px; }\n" +
                    "        .modal-box input {\n" +
                    "            width: 100%;\n" +
                    "            padding: 10px;\n" +
                    "            border-radius: 8px;\n" +
                    "            border: 1px solid var(--border-light);\n" +
                    "            background: var(--bg);\n" +
                    "            color: var(--text-primary);\n" +
                    "            font-size: 14px;\n" +
                    "            outline: none;\n" +
                    "        }\n" +
                    "        .modal-box input:focus { border-color: #3498db; }\n" +
                    "        .modal-actions { display: flex; gap: 12px; margin-top: 16px; justify-content: flex-end; }\n" +
                    "        .modal-actions button { padding: 8px 20px; border-radius: 8px; border: none; cursor: pointer; font-weight: 500; transition: 0.2s; }\n" +
                    "        .modal-actions .btn-confirm { background: #3498db; color: white; }\n" +
                    "        .modal-actions .btn-confirm:hover { background: #2980b9; }\n" +
                    "        .modal-actions .btn-cancel { background: var(--toggle-off); color: var(--text-primary); }\n" +
                    "        .modal-actions .btn-cancel:hover { background: #b0b0b0; }\n" +
                    "\n" +
                    "        @media (max-width: 768px) {\n" +
                    "            #sidebar { width: 60px; padding: 16px 8px; }\n" +
                    "            .logo { display: none; }\n" +
                    "            #category-list li { padding: 10px; text-align: center; font-size: 0; }\n" +
                    "            #category-list li i { font-size: 20px; }\n" +
                    "            #category-list li.active { background: rgba(52,152,219,0.2); }\n" +
                    "            #content { padding: 20px; }\n" +
                    "            #module-grid { grid-template-columns: repeat(2, 1fr); }\n" +
                    "        }\n" +
                    "        @media (max-width: 480px) {\n" +
                    "            #module-grid { grid-template-columns: 1fr; }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div id=\"app\">\n" +
                    "    <aside id=\"sidebar\">\n" +
                    "        <div class=\"logo\">Myau</div>\n" +
                    "        <nav>\n" +
                    "            <ul id=\"category-list\"></ul>\n" +
                    "        </nav>\n" +
                    "    </aside>\n" +
                    "    <main id=\"content\">\n" +
                    "        <div id=\"module-grid\"></div>\n" +
                    "    </main>\n" +
                    "</div>\n" +
                    "<button id=\"theme-toggle\" title=\"Toggle dark mode\">\n" +
                    "    <i class=\"fas fa-sun\"></i>\n" +
                    "</button>\n" +
                    "<!-- Toast 容器 -->\n" +
                    "<div id=\"toast-container\"></div>\n" +
                    "<!-- 自定义模态框 -->\n" +
                    "<div class=\"modal-overlay\" id=\"bindModal\">\n" +
                    "    <div class=\"modal-box\">\n" +
                    "        <h3>Enter Key Name</h3>\n" +
                    "        <input type=\"text\" id=\"bindInput\" placeholder=\"e.g. R, L, MOUSE1, 0 for none\" autofocus>\n" +
                    "        <div class=\"modal-actions\">\n" +
                    "            <button class=\"btn-cancel\" id=\"bindCancel\">Cancel</button>\n" +
                    "            <button class=\"btn-confirm\" id=\"bindConfirm\">Bind</button>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</div>\n" +
                    "<script>\n" +
                    "    const API_BASE = '/api';\n" +
                    "    let currentCategory = 'Combat';\n" +
                    "    let modulesData = {};\n" +
                    "    let isDark = false;\n" +
                    "    let pendingBindModule = null;\n" +
                    "    const iconMap = { 'Combat': 'crosshairs', 'Movement': 'running', 'Render': 'palette', 'Player': 'user', 'Misc': 'cubes' };\n" +
                    "\n" +
                    "    const modalOverlay = document.getElementById('bindModal');\n" +
                    "    const bindInput = document.getElementById('bindInput');\n" +
                    "    const bindConfirm = document.getElementById('bindConfirm');\n" +
                    "    const bindCancel = document.getElementById('bindCancel');\n" +
                    "    const toastContainer = document.getElementById('toast-container');\n" +
                    "\n" +
                    "    function showToast(message, type = 'info') {\n" +
                    "        const toast = document.createElement('div');\n" +
                    "        toast.className = `toast ${type}`;\n" +
                    "        toast.textContent = message;\n" +
                    "        toastContainer.appendChild(toast);\n" +
                    "        setTimeout(() => {\n" +
                    "            toast.style.animation = 'toastOut 0.3s ease forwards';\n" +
                    "            setTimeout(() => toast.remove(), 300);\n" +
                    "        }, 3000);\n" +
                    "    }\n" +
                    "\n" +
                    "    function showBindModal(moduleName) {\n" +
                    "        pendingBindModule = moduleName;\n" +
                    "        bindInput.value = '';\n" +
                    "        modalOverlay.classList.add('active');\n" +
                    "        setTimeout(() => bindInput.focus(), 100);\n" +
                    "    }\n" +
                    "    function hideBindModal() { modalOverlay.classList.remove('active'); pendingBindModule = null; }\n" +
                    "    bindConfirm.addEventListener('click', () => {\n" +
                    "        const key = bindInput.value.trim();\n" +
                    "        if (key && pendingBindModule) { bindKey(pendingBindModule, key); }\n" +
                    "        hideBindModal();\n" +
                    "    });\n" +
                    "    bindCancel.addEventListener('click', hideBindModal);\n" +
                    "    modalOverlay.addEventListener('click', (e) => { if (e.target === modalOverlay) hideBindModal(); });\n" +
                    "    bindInput.addEventListener('keydown', (e) => {\n" +
                    "        if (e.key === 'Enter') bindConfirm.click();\n" +
                    "        if (e.key === 'Escape') hideBindModal();\n" +
                    "    });\n" +
                    "\n" +
                    "    document.getElementById('theme-toggle').addEventListener('click', () => {\n" +
                    "        isDark = !isDark;\n" +
                    "        document.body.classList.toggle('dark', isDark);\n" +
                    "        const icon = document.querySelector('#theme-toggle i');\n" +
                    "        icon.className = isDark ? 'fas fa-moon' : 'fas fa-sun';\n" +
                    "        localStorage.setItem('myau-dark-mode', isDark ? 'true' : 'false');\n" +
                    "    });\n" +
                    "\n" +
                    "    (function loadTheme() {\n" +
                    "        const saved = localStorage.getItem('myau-dark-mode');\n" +
                    "        if (saved === 'true') {\n" +
                    "            isDark = true;\n" +
                    "            document.body.classList.add('dark');\n" +
                    "            document.querySelector('#theme-toggle i').className = 'fas fa-moon';\n" +
                    "        }\n" +
                    "    })();\n" +
                    "\n" +
                    "    async function fetchModules() {\n" +
                    "        const res = await fetch(`${API_BASE}?action=getModules`);\n" +
                    "        return await res.json();\n" +
                    "    }\n" +
                    "\n" +
                    "    function renderCategories(categories) {\n" +
                    "        const list = document.getElementById('category-list');\n" +
                    "        list.innerHTML = '';\n" +
                    "        categories.forEach(cat => {\n" +
                    "            const li = document.createElement('li');\n" +
                    "            li.dataset.category = cat;\n" +
                    "            li.innerHTML = `<i class=\"fas fa-${iconMap[cat] || 'folder'}\"></i> ${cat}`;\n" +
                    "            if (cat === currentCategory) li.classList.add('active');\n" +
                    "            li.addEventListener('click', () => selectCategory(cat));\n" +
                    "            list.appendChild(li);\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    function selectCategory(cat) {\n" +
                    "        currentCategory = cat;\n" +
                    "        document.querySelectorAll('#category-list li').forEach(el => {\n" +
                    "            el.classList.toggle('active', el.dataset.category === cat);\n" +
                    "        });\n" +
                    "        refreshData(false);\n" +
                    "    }\n" +
                    "\n" +
                    "    function renderModules(modules) {\n" +
                    "        const grid = document.getElementById('module-grid');\n" +
                    "        grid.innerHTML = '';\n" +
                    "        modules.forEach((mod, index) => {\n" +
                    "            const card = createModuleCard(mod, index);\n" +
                    "            grid.appendChild(card);\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    function createModuleCard(mod, index) {\n" +
                    "        const card = document.createElement('div');\n" +
                    "        card.className = 'module-card';\n" +
                    "        card.dataset.module = mod.name;\n" +
                    "        const header = document.createElement('div');\n" +
                    "        header.className = 'card-header';\n" +
                    "        const name = document.createElement('span');\n" +
                    "        name.className = 'module-name';\n" +
                    "        name.textContent = mod.displayName || mod.name;\n" +
                    "        const controls = document.createElement('div');\n" +
                    "        controls.className = 'module-controls';\n" +
                    "        const bindBtn = document.createElement('button');\n" +
                    "        bindBtn.className = 'bind-btn';\n" +
                    "        bindBtn.innerHTML = `<i class=\"fas fa-key\"></i>`;\n" +
                    "        bindBtn.title = 'Bind Key';\n" +
                    "        bindBtn.addEventListener('click', (e) => {\n" +
                    "            e.stopPropagation();\n" +
                    "            showBindModal(mod.name);\n" +
                    "        });\n" +
                    "        controls.appendChild(bindBtn);\n" +
                    "        const toggle = document.createElement('div');\n" +
                    "        toggle.className = `toggle-switch${mod.enabled ? ' active' : ''}`;\n" +
                    "        toggle.addEventListener('click', (e) => {\n" +
                    "            e.stopPropagation();\n" +
                    "            toggleModule(mod.name, toggle);\n" +
                    "        });\n" +
                    "        controls.appendChild(toggle);\n" +
                    "        header.appendChild(name);\n" +
                    "        header.appendChild(controls);\n" +
                    "        card.appendChild(header);\n" +
                    "        const propsDiv = document.createElement('div');\n" +
                    "        propsDiv.className = 'module-properties';\n" +
                    "        if (mod.properties && mod.properties.length > 0) {\n" +
                    "            mod.properties.forEach(prop => {\n" +
                    "                const row = createPropertyRow(mod.name, prop);\n" +
                    "                propsDiv.appendChild(row);\n" +
                    "            });\n" +
                    "        } else {\n" +
                    "            const empty = document.createElement('div');\n" +
                    "            empty.className = 'property-row';\n" +
                    "            empty.innerHTML = '<span style=\"color:#888;font-size:12px;\">No properties</span>';\n" +
                    "            propsDiv.appendChild(empty);\n" +
                    "        }\n" +
                    "        card.appendChild(propsDiv);\n" +
                    "        card.addEventListener('contextmenu', (e) => {\n" +
                    "            e.preventDefault();\n" +
                    "            propsDiv.classList.toggle('open');\n" +
                    "        });\n" +
                    "        return card;\n" +
                    "    }\n" +
                    "\n" +
                    "    function createPropertyRow(moduleName, prop) {\n" +
                    "        const row = document.createElement('div');\n" +
                    "        row.className = 'property-row';\n" +
                    "        const nameSpan = document.createElement('span');\n" +
                    "        nameSpan.className = 'property-name';\n" +
                    "        nameSpan.textContent = prop.name;\n" +
                    "        const valueDiv = document.createElement('div');\n" +
                    "        valueDiv.className = 'property-value';\n" +
                    "        if (prop.type === 'Boolean') {\n" +
                    "            const toggle = document.createElement('div');\n" +
                    "            toggle.className = `property-toggle${prop.value ? ' active' : ''}`;\n" +
                    "            toggle.addEventListener('click', (e) => {\n" +
                    "                e.stopPropagation();\n" +
                    "                const newVal = !prop.value;\n" +
                    "                setProperty(moduleName, prop.name, String(newVal));\n" +
                    "            });\n" +
                    "            valueDiv.appendChild(toggle);\n" +
                    "        } else if (prop.type === 'Int' || prop.type === 'Float' || prop.type === 'Percent') {\n" +
                    "            const range = document.createElement('input');\n" +
                    "            range.type = 'range';\n" +
                    "            range.min = prop.min || 0;\n" +
                    "            range.max = prop.max || 100;\n" +
                    "            range.step = prop.step || 1;\n" +
                    "            range.value = prop.value;\n" +
                    "            range.dataset.prop = prop.name;\n" +
                    "            const valDisplay = document.createElement('span');\n" +
                    "            valDisplay.textContent = prop.value;\n" +
                    "            range.addEventListener('input', () => {\n" +
                    "                valDisplay.textContent = range.value;\n" +
                    "            });\n" +
                    "            range.addEventListener('change', () => {\n" +
                    "                setProperty(moduleName, prop.name, range.value);\n" +
                    "            });\n" +
                    "            valueDiv.appendChild(range);\n" +
                    "            valueDiv.appendChild(valDisplay);\n" +
                    "        } else if (prop.type === 'Mode') {\n" +
                    "            const selector = document.createElement('div');\n" +
                    "            selector.className = 'mode-selector';\n" +
                    "            prop.modes.forEach((mode, idx) => {\n" +
                    "                const opt = document.createElement('span');\n" +
                    "                opt.className = `mode-option${idx === prop.value ? ' active' : ''}`;\n" +
                    "                opt.textContent = mode;\n" +
                    "                opt.addEventListener('click', () => {\n" +
                    "                    setProperty(moduleName, prop.name, mode);\n" +
                    "                });\n" +
                    "                selector.appendChild(opt);\n" +
                    "            });\n" +
                    "            valueDiv.appendChild(selector);\n" +
                    "        } else if (prop.type === 'Color') {\n" +
                    "            const colorInput = document.createElement('input');\n" +
                    "            colorInput.type = 'color';\n" +
                    "            colorInput.value = prop.value || '#ffffff';\n" +
                    "            colorInput.dataset.prop = prop.name;\n" +
                    "            colorInput.addEventListener('change', () => {\n" +
                    "                setProperty(moduleName, prop.name, colorInput.value);\n" +
                    "            });\n" +
                    "            valueDiv.appendChild(colorInput);\n" +
                    "        } else if (prop.type === 'Text') {\n" +
                    "            const input = document.createElement('input');\n" +
                    "            input.type = 'text';\n" +
                    "            input.value = prop.value || '';\n" +
                    "            input.style.width = '120px';\n" +
                    "            input.addEventListener('change', () => {\n" +
                    "                setProperty(moduleName, prop.name, input.value);\n" +
                    "            });\n" +
                    "            valueDiv.appendChild(input);\n" +
                    "        }\n" +
                    "        row.appendChild(nameSpan);\n" +
                    "        row.appendChild(valueDiv);\n" +
                    "        return row;\n" +
                    "    }\n" +
                    "\n" +
                    "    async function toggleModule(name, toggleElement) {\n" +
                    "        const res = await fetch(`${API_BASE}?action=toggleModule&module=${encodeURIComponent(name)}`);\n" +
                    "        const data = await res.json();\n" +
                    "        if (data.success) {\n" +
                    "            refreshData(false);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    async function setProperty(module, prop, value) {\n" +
                    "        const res = await fetch(`${API_BASE}?action=setProperty&module=${encodeURIComponent(module)}&prop=${encodeURIComponent(prop)}&value=${encodeURIComponent(value)}`);\n" +
                    "        const data = await res.json();\n" +
                    "        if (data.success) {\n" +
                    "            refreshData(false);\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    async function bindKey(module, keyName) {\n" +
                    "        const res = await fetch(`${API_BASE}?action=bindKey&module=${encodeURIComponent(module)}&key=${encodeURIComponent(keyName)}`);\n" +
                    "        const data = await res.json();\n" +
                    "        if (data.success) {\n" +
                    "            showToast('Key bound successfully!', 'success');\n" +
                    "            refreshData(false);\n" +
                    "        } else {\n" +
                    "            showToast('Failed to bind key: ' + (data.error || 'unknown error'), 'error');\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    let expandedModules = new Set();\n" +
                    "    function saveExpandedState() {\n" +
                    "        expandedModules.clear();\n" +
                    "        document.querySelectorAll('.module-card').forEach(card => {\n" +
                    "            const props = card.querySelector('.module-properties');\n" +
                    "            if (props && props.classList.contains('open')) {\n" +
                    "                expandedModules.add(card.dataset.module);\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "    function restoreExpandedState() {\n" +
                    "        document.querySelectorAll('.module-card').forEach(card => {\n" +
                    "            const props = card.querySelector('.module-properties');\n" +
                    "            if (props && expandedModules.has(card.dataset.module)) {\n" +
                    "                props.classList.add('open');\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    // 滚动位置保存和恢复\n" +
                    "    let scrollPositions = {};\n" +
                    "    function saveScrollPositions() {\n" +
                    "        scrollPositions = {};\n" +
                    "        document.querySelectorAll('.module-card').forEach(card => {\n" +
                    "            const selector = card.querySelector('.mode-selector');\n" +
                    "            if (selector) {\n" +
                    "                const modName = card.dataset.module;\n" +
                    "                scrollPositions[modName] = selector.scrollLeft;\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "    function restoreScrollPositions() {\n" +
                    "        document.querySelectorAll('.module-card').forEach(card => {\n" +
                    "            const modName = card.dataset.module;\n" +
                    "            if (modName && scrollPositions[modName] !== undefined) {\n" +
                    "                const selector = card.querySelector('.mode-selector');\n" +
                    "                if (selector) {\n" +
                    "                    selector.scrollLeft = scrollPositions[modName];\n" +
                    "                }\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    async function refreshData(showLoading = false) {\n" +
                    "        // 保存当前滚动位置和展开状态\n" +
                    "        saveScrollPositions();\n" +
                    "        saveExpandedState();\n" +
                    "\n" +
                    "        const data = await fetchModules();\n" +
                    "        modulesData = data.modules || {};\n" +
                    "        if (!modulesData[currentCategory]) {\n" +
                    "            const firstCat = Object.keys(modulesData)[0] || 'Combat';\n" +
                    "            currentCategory = firstCat;\n" +
                    "        }\n" +
                    "        renderCategories(Object.keys(modulesData));\n" +
                    "        renderModules(modulesData[currentCategory] || []);\n" +
                    "\n" +
                    "        // 恢复滚动位置和展开状态\n" +
                    "        restoreScrollPositions();\n" +
                    "        restoreExpandedState();\n" +
                    "    }\n" +
                    "\n" +
                    "    // 页面初始化，不显示加载遮罩\n" +
                    "    refreshData(false);\n" +
                    "\n" +
                    "    // 每 3 秒静默刷新一次，减少干扰\n" +
                    "    setInterval(() => refreshData(false), 3000);\n" +
                    "</script>\n" +
                    "</body>\n" +
                    "</html>";

    public static synchronized void startServer() {
        if (running) return;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StaticHandler());
            server.createContext("/api", new ApiHandler());
            server.setExecutor(null);
            server.start();
            running = true;
            System.out.println("[Myau] WebUI started at http://localhost:" + PORT + "/");
            try {
                Desktop.getDesktop().browse(URI.create("http://localhost:" + PORT + "/"));
            } catch (Exception ignored) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            running = false;
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                sendHtml(exchange, INDEX_HTML);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }

        private void sendHtml(HttpExchange exchange, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String action = params.getOrDefault("action", "");
            String response = "{}";

            switch (action) {
                case "getModules":
                    response = getModulesJson();
                    break;
                case "toggleModule":
                    response = toggleModule(params.get("module"));
                    break;
                case "setProperty":
                    response = setProperty(params.get("module"), params.get("prop"), params.get("value"));
                    break;
                case "bindKey":
                    response = bindKey(params.get("module"), params.get("key"));
                    break;
                default:
                    response = "{\"error\":\"unknown action\"}";
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query == null) return map;
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    try {
                        map.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
                    } catch (java.io.UnsupportedEncodingException e) {
                        map.put(kv[0], kv[1]);
                    }
                }
            }
            return map;
        }

        private String getModulesJson() {
            Map<String, List<Module>> categories = new LinkedHashMap<>();
            categories.put("Combat", Arrays.asList(
                    Myau.moduleManager.getModule(AimAssist.class),
                    Myau.moduleManager.getModule(AutoClicker.class),
                    Myau.moduleManager.getModule(KillAura.class),
                    Myau.moduleManager.getModule(Wtap.class),
                    Myau.moduleManager.getModule(Velocity.class),
                    Myau.moduleManager.getModule(Reach.class),
                    Myau.moduleManager.getModule(TargetStrafe.class),
                    Myau.moduleManager.getModule(NoHitDelay.class),
                    Myau.moduleManager.getModule(AntiFireball.class),
                    Myau.moduleManager.getModule(LagRange.class),
                    Myau.moduleManager.getModule(HitBox.class),
                    Myau.moduleManager.getModule(MoreKB.class),
                    Myau.moduleManager.getModule(HitSelect.class)
            ));
            categories.put("Movement", Arrays.asList(
                    Myau.moduleManager.getModule(AntiAFK.class),
                    Myau.moduleManager.getModule(Fly.class),
                    Myau.moduleManager.getModule(Speed.class),
                    Myau.moduleManager.getModule(LongJump.class),
                    Myau.moduleManager.getModule(Sprint.class),
                    Myau.moduleManager.getModule(Jesus.class),
                    Myau.moduleManager.getModule(Blink.class),
                    Myau.moduleManager.getModule(NoFall.class),
                    Myau.moduleManager.getModule(NoSlow.class),
                    Myau.moduleManager.getModule(KeepSprint.class),
                    Myau.moduleManager.getModule(Eagle.class),
                    Myau.moduleManager.getModule(NoJumpDelay.class),
                    Myau.moduleManager.getModule(AntiVoid.class)
            ));
            categories.put("Render", Arrays.asList(
                    Myau.moduleManager.getModule(ESP.class),
                    Myau.moduleManager.getModule(Chams.class),
                    Myau.moduleManager.getModule(FullBright.class),
                    Myau.moduleManager.getModule(Tracers.class),
                    Myau.moduleManager.getModule(NameTags.class),
                    Myau.moduleManager.getModule(Xray.class),
                    Myau.moduleManager.getModule(TargetHUD.class),
                    Myau.moduleManager.getModule(Indicators.class),
                    Myau.moduleManager.getModule(BedESP.class),
                    Myau.moduleManager.getModule(ItemESP.class),
                    Myau.moduleManager.getModule(ViewClip.class),
                    Myau.moduleManager.getModule(NoHurtCam.class),
                    Myau.moduleManager.getModule(HUD.class),
                    Myau.moduleManager.getModule(GuiModule.class),
                    Myau.moduleManager.getModule(ChestESP.class),
                    Myau.moduleManager.getModule(Trajectories.class),
                    Myau.moduleManager.getModule(Notifications.class)
            ));
            categories.put("Player", Arrays.asList(
                    Myau.moduleManager.getModule(AutoHeal.class),
                    Myau.moduleManager.getModule(AutoTool.class),
                    Myau.moduleManager.getModule(ChestStealer.class),
                    Myau.moduleManager.getModule(InvManager.class),
                    Myau.moduleManager.getModule(InvWalk.class),
                    Myau.moduleManager.getModule(Scaffold.class),
                    Myau.moduleManager.getModule(AutoBlockIn.class),
                    Myau.moduleManager.getModule(SpeedMine.class),
                    Myau.moduleManager.getModule(FastPlace.class),
                    Myau.moduleManager.getModule(GhostHand.class),
                    Myau.moduleManager.getModule(MCF.class),
                    Myau.moduleManager.getModule(AntiDebuff.class),
                    Myau.moduleManager.getModule(myau.module.modules.Timer.class)
            ));
            categories.put("Misc", Arrays.asList(
                    Myau.moduleManager.getModule(Spammer.class),
                    Myau.moduleManager.getModule(BedNuker.class),
                    Myau.moduleManager.getModule(BedTracker.class),
                    Myau.moduleManager.getModule(LightningTracker.class),
                    Myau.moduleManager.getModule(NoRotate.class),
                    Myau.moduleManager.getModule(NickHider.class),
                    Myau.moduleManager.getModule(AntiObbyTrap.class),
                    Myau.moduleManager.getModule(AntiObfuscate.class),
                    Myau.moduleManager.getModule(AutoAnduril.class),
                    Myau.moduleManager.getModule(InventoryClicker.class)
            ));

            JsonObject root = new JsonObject();
            JsonObject modulesObj = new JsonObject();
            for (Map.Entry<String, List<Module>> entry : categories.entrySet()) {
                JsonArray arr = new JsonArray();
                for (Module mod : entry.getValue()) {
                    if (mod == null) continue;
                    JsonObject modJson = new JsonObject();
                    modJson.addProperty("name", mod.getName());
                    modJson.addProperty("displayName", mod.getName());
                    modJson.addProperty("enabled", mod.isEnabled());
                    JsonArray props = new JsonArray();
                    List<Property<?>> propsList = Myau.propertyManager.properties.get(mod.getClass());
                    if (propsList != null) {
                        for (Property<?> p : propsList) {
                            if (!p.isVisible()) continue;
                            JsonObject propJson = new JsonObject();
                            propJson.addProperty("name", p.getName());
                            if (p instanceof BooleanProperty) {
                                propJson.addProperty("type", "Boolean");
                                propJson.addProperty("value", ((BooleanProperty) p).getValue());
                            } else if (p instanceof IntProperty) {
                                propJson.addProperty("type", "Int");
                                propJson.addProperty("value", ((IntProperty) p).getValue());
                                propJson.addProperty("min", ((IntProperty) p).getMinimum());
                                propJson.addProperty("max", ((IntProperty) p).getMaximum());
                                propJson.addProperty("step", 1);
                            } else if (p instanceof FloatProperty) {
                                propJson.addProperty("type", "Float");
                                propJson.addProperty("value", ((FloatProperty) p).getValue());
                                propJson.addProperty("min", ((FloatProperty) p).getMinimum());
                                propJson.addProperty("max", ((FloatProperty) p).getMaximum());
                                propJson.addProperty("step", 0.1);
                            } else if (p instanceof PercentProperty) {
                                propJson.addProperty("type", "Percent");
                                propJson.addProperty("value", ((PercentProperty) p).getValue());
                                propJson.addProperty("min", ((PercentProperty) p).getMinimum());
                                propJson.addProperty("max", ((PercentProperty) p).getMaximum());
                                propJson.addProperty("step", 1);
                            } else if (p instanceof ModeProperty) {
                                propJson.addProperty("type", "Mode");
                                propJson.addProperty("value", ((ModeProperty) p).getValue());
                                JsonArray modesArr = new JsonArray();
                                for (String m : ((ModeProperty) p).getModes()) {
                                    modesArr.add(new JsonPrimitive(m));
                                }
                                propJson.add("modes", modesArr);
                            } else if (p instanceof ColorProperty) {
                                propJson.addProperty("type", "Color");
                                int color = ((ColorProperty) p).getValue();
                                propJson.addProperty("value", String.format("#%06X", 0xFFFFFF & color));
                            } else if (p instanceof TextProperty) {
                                propJson.addProperty("type", "Text");
                                propJson.addProperty("value", ((TextProperty) p).getValue());
                            }
                            props.add(propJson);
                        }
                    }
                    modJson.add("properties", props);
                    arr.add(modJson);
                }
                modulesObj.add(entry.getKey(), arr);
            }
            root.add("modules", modulesObj);
            return GSON.toJson(root);
        }

        private String toggleModule(String name) {
            Module mod = Myau.moduleManager.getModule(name);
            if (mod == null) return "{\"error\":\"module not found\"}";
            mod.toggle();
            return "{\"success\":true,\"enabled\":" + mod.isEnabled() + "}";
        }

        private String setProperty(String moduleName, String propName, String value) {
            Module mod = Myau.moduleManager.getModule(moduleName);
            if (mod == null) return "{\"error\":\"module not found\"}";
            Property<?> prop = Myau.propertyManager.getProperty(mod, propName);
            if (prop == null) return "{\"error\":\"property not found\"}";
            if (prop.parseString(value)) {
                if (prop instanceof IntProperty) {
                    return "{\"success\":true,\"value\":\"" + ((IntProperty) prop).getValue() + "\"}";
                } else if (prop instanceof FloatProperty) {
                    return "{\"success\":true,\"value\":\"" + ((FloatProperty) prop).getValue() + "\"}";
                } else if (prop instanceof PercentProperty) {
                    return "{\"success\":true,\"value\":\"" + ((PercentProperty) prop).getValue() + "\"}";
                } else if (prop instanceof BooleanProperty) {
                    return "{\"success\":true,\"value\":\"" + ((BooleanProperty) prop).getValue() + "\"}";
                } else if (prop instanceof ModeProperty) {
                    return "{\"success\":true,\"value\":\"" + ((ModeProperty) prop).getModeString() + "\"}";
                } else if (prop instanceof ColorProperty) {
                    int color = ((ColorProperty) prop).getValue();
                    return "{\"success\":true,\"value\":\"" + String.format("#%06X", 0xFFFFFF & color) + "\"}";
                } else {
                    return "{\"success\":true,\"value\":\"" + prop.formatValue() + "\"}";
                }
            } else {
                return "{\"error\":\"invalid value\"}";
            }
        }

        private String bindKey(String moduleName, String keyName) {
            Module mod = Myau.moduleManager.getModule(moduleName);
            if (mod == null) return "{\"error\":\"module not found\"}";
            int keyCode;
            try {
                keyCode = Integer.parseInt(keyName);
            } catch (NumberFormatException e) {
                keyCode = Keyboard.getKeyIndex(keyName.toUpperCase());
                if (keyCode == 0) {
                    Map<String, Integer> mouseMap = new HashMap<>();
                    mouseMap.put("LMB", -100);
                    mouseMap.put("RMB", -99);
                    mouseMap.put("MMB", -98);
                    mouseMap.put("MOUSE3", -97);
                    mouseMap.put("MOUSE4", -96);
                    mouseMap.put("MOUSE5", -95);
                    if (keyName.toUpperCase().startsWith("MOUSE")) {
                        try {
                            int btn = Integer.parseInt(keyName.substring(5));
                            keyCode = btn - 100;
                        } catch (NumberFormatException ex) {
                            keyCode = mouseMap.getOrDefault(keyName.toUpperCase(), 0);
                        }
                    } else {
                        keyCode = mouseMap.getOrDefault(keyName.toUpperCase(), 0);
                    }
                    if (keyCode == 0) {
                        return "{\"error\":\"unknown key name\"}";
                    }
                }
            }
            mod.setKey(keyCode);
            return "{\"success\":true}";
        }
    }
}