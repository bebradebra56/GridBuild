package com.gridibuild.sfobud.lgte.presentation.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication

class GridBuildVi(
    private val gridBuildContext: Context,
    private val gridBuildCallback: GridBuildCallBack,
    private val gridBuildWindow: Window
) : WebView(gridBuildContext) {
    private var gridBuildFileChooserHandler: ((ValueCallback<Array<Uri>>?) -> Unit)? = null
    fun gridBuildSetFileChooserHandler(handler: (ValueCallback<Array<Uri>>?) -> Unit) {
        this.gridBuildFileChooserHandler = handler
    }

    init {
        val webSettings = settings
        webSettings.apply {
            setSupportMultipleWindows(true)
            allowFileAccess = true
            allowContentAccess = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            userAgentString =
                WebSettings.getDefaultUserAgent(gridBuildContext).replace("; wv)", "")
                    .replace("Version/4.0 ", "")
            @SuppressLint("SetJavaScriptEnabled")
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }


        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        super.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val link = request?.url?.toString() ?: ""

                return if (request?.isRedirect == true) {
                    view?.loadUrl(request?.url.toString())
                    true
                } else if (URLUtil.isNetworkUrl(link)) {
                    false
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    try {
                        gridBuildContext.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            gridBuildContext,
                            "This application not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
            }


            override fun onPageFinished(view: WebView?, url: String?) {
                CookieManager.getInstance().flush()
                if (url?.contains("ninecasino") == true) {
                    GridBuildApplication.gridBuildInputMode =
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                    Log.d(
                        GridBuildApplication.GRID_BUILD_MAIN_TAG,
                        "onPageFinished : ${GridBuildApplication.gridBuildInputMode}"
                    )
                    gridBuildWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                    gridBuildEnableStableInputScroll()
                } else {
                    GridBuildApplication.gridBuildInputMode =
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    Log.d(
                        GridBuildApplication.GRID_BUILD_MAIN_TAG,
                        "onPageFinished : ${GridBuildApplication.gridBuildInputMode}"
                    )
                    gridBuildWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            }


        })

        super.setWebChromeClient(object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?,
            ): Boolean {
                gridBuildFileChooserHandler?.invoke(filePathCallback)
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                gridBuildHandleCreateWebWindowRequest(resultMsg)
                return true
            }
        })
    }

    // ГИБРИДНАЯ ФУНКЦИЯ - объединяет лучшее из обеих подходов
    private fun gridBuildEnableStableInputScroll() {
        evaluateJavascript(
            """
        (function() {
            if (window.__stableInputScrollInstalled) return;
            window.__stableInputScrollInstalled = true;

            let initialVH = window.innerHeight;
            let focusedElement = null;
            let activeTransform = 0;

            console.log('[KB-HYBRID] Installed, initialVH:', initialVH);

            // Находим родительский скроллируемый контейнер
            function findScrollParent(el) {
                let node = el ? el.parentElement : null;
                while (node && node !== document.body && node !== document.documentElement) {
                    const style = window.getComputedStyle(node);
                    const overflow = style.overflowY;
                    if (overflow === 'auto' || overflow === 'scroll') {
                        return node;
                    }
                    node = node.parentElement;
                }
                return document.scrollingElement || document.documentElement;
            }

            // Определяем высоту клавиатуры ТОЧНО
            function getKeyboardHeight() {
                const currentVH = window.innerHeight;
                const kbHeight = initialVH - currentVH;
                // Если клавиатура еще не появилась, используем оценку
                return kbHeight > 50 ? kbHeight : Math.floor(initialVH * 0.4);
            }

            // Проверяем видимость элемента с учетом реальной клавиатуры
            function isElementVisible(el) {
                const rect = el.getBoundingClientRect();
                const estimatedKB = getKeyboardHeight();
                const currentVH = window.innerHeight;
                const visibleHeight = currentVH - estimatedKB;
                const padding = 30;
                const safeBottom = visibleHeight - padding;
                const minTop = 10; // минимальный отступ от верха

                const isBottomVisible = rect.bottom <= safeBottom;
                const isTopVisible = rect.top >= minTop;

                console.log('[KB-HYBRID] Check visibility: top=' + rect.top + ' bottom=' + rect.bottom + ' safeBottom=' + safeBottom);

                return isBottomVisible && isTopVisible;
            }

            // Скроллим к элементу (БЫСТРО без smooth)
            function scrollToElement(el) {
                const parent = findScrollParent(el);
                const rect = el.getBoundingClientRect();
                const parentRect = parent.getBoundingClientRect();

                const offset = 50;
                const target = rect.top - parentRect.top + parent.scrollTop - offset;

                console.log('[KB-HYBRID] Scrolling to:', target);

                // INSTANT scroll для быстрого завершения
                parent.scrollTo({ 
                    top: Math.max(0, target), 
                    behavior: 'auto' 
                });
            }

            // ПРОСТОЙ И НАДЕЖНЫЙ transform
            function applyTransform(el) {
                const rect = el.getBoundingClientRect();
                const estimatedKB = getKeyboardHeight();
                const currentVH = window.innerHeight;
                const visibleHeight = currentVH - estimatedKB;
                const padding = 20;
                const safeBottom = visibleHeight - padding;
                const minTopMargin = 40; // минимум от верха

                console.log('[KB-HYBRID] KB=' + estimatedKB + ' visibleH=' + visibleHeight + ' elTop=' + rect.top + ' elBottom=' + rect.bottom + ' safe=' + safeBottom);

                // Вычисляем transform
                let shift = 0;
                if (rect.bottom > safeBottom) {
                    shift = rect.bottom - safeBottom + 20; // +20 для запаса
                }

                // Защита от ухода за верх
                const topAfter = rect.top - shift;
                if (topAfter < minTopMargin) {
                    shift = Math.max(0, rect.top - minTopMargin);
                }

                // Ограничение
                shift = Math.min(shift, currentVH * 0.7);

                activeTransform = shift;
                console.log('[KB-HYBRID] >>> TRANSFORM:', shift);
                
                // Применяем на documentElement с ОЧЕНЬ ПЛАВНОЙ анимацией
                document.documentElement.style.transition = 'transform 0.35s ease-out';
                document.documentElement.style.transform = shift > 0 ? 'translateY(-' + shift + 'px)' : '';
            }

            // Главная функция
            function ensureInputVisible(el) {
                if (!el) return;

                console.log('[KB-HYBRID] ===== START =====');

                // Шаг 1: БЫСТРЫЙ скролл (instant)
                scrollToElement(el);

                // Шаг 2: ПЛАВНЫЙ transform через небольшую задержку
                // Даем время браузеру обработать скролл
                setTimeout(() => applyTransform(el), 150);
            }

            // Сброс позиции
            function resetPosition() {
                console.log('[KB-HYBRID] >>> RESET');
                
                document.documentElement.style.transition = 'transform 0.2s ease-out';
                document.documentElement.style.transform = '';
                activeTransform = 0;
                
                setTimeout(() => {
                    document.documentElement.style.transition = '';
                }, 200);
            }

            // Расписание с несколькими попытками для надежности
            function scheduleEnsureVisible(el) {
                ensureInputVisible(el);
                setTimeout(() => ensureInputVisible(el), 100);
                setTimeout(() => ensureInputVisible(el), 250);
                setTimeout(() => ensureInputVisible(el), 400);
            }

            // События
            document.addEventListener('focusin', function(e) {
                const el = e.target;
                if (!el || (el.tagName !== 'INPUT' && el.tagName !== 'TEXTAREA')) {
                    return;
                }

                console.log('[KB-HYBRID] ===== FOCUS:', el.tagName, el.name || el.id, '=====');
                focusedElement = el;

                // Небольшая задержка для появления клавиатуры
                setTimeout(() => scheduleEnsureVisible(el), 300);
            });

            document.addEventListener('focusout', function() {
                console.log('[KB-HYBRID] ===== BLUR =====');
                focusedElement = null;
                setTimeout(resetPosition, 150);
            });

            // Resize
            let resizeTimer;
            window.addEventListener('resize', function() {
                clearTimeout(resizeTimer);
                resizeTimer = setTimeout(function() {
                    const newVH = window.innerHeight;
                    console.log('[KB-HYBRID] Resize, VH:', newVH);

                    if (newVH > initialVH - 50) {
                        // Клавиатура закрылась
                        if (newVH > initialVH) {
                            initialVH = newVH;
                        }
                        resetPosition();
                    } else if (focusedElement) {
                        // Клавиатура изменила размер
                        scheduleEnsureVisible(focusedElement);
                    }
                }, 150);
            });

            console.log('[KB-HYBRID] Ready! Hybrid scroll+transform approach enabled.');
        })();
        """.trimIndent(),
            null
        )
    }


    fun gridBuildFLoad(link: String) {
        super.loadUrl(link)
    }

    private fun gridBuildHandleCreateWebWindowRequest(resultMsg: Message?) {
        if (resultMsg == null) return
        if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport) {
            val transport = resultMsg.obj as WebView.WebViewTransport
            val windowWebView =
                GridBuildVi(gridBuildContext, gridBuildCallback, gridBuildWindow)
            transport.webView = windowWebView
            resultMsg.sendToTarget()
            gridBuildCallback.gridBuildHandleCreateWebWindowRequest(windowWebView)
        }
    }

}