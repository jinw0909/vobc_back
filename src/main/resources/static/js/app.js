// /**
//  * 붙여넣기 시 HTML + 미디어 유지
//  * - img / video / audio / iframe 유지
//  * - script / 이벤트 속성 제거
//  * - Summernote용
//  */
// window.handlePasteMedia = function handlePasteMedia(e) {
//     const oe = e.originalEvent || e;
//     const cd = oe.clipboardData;
//     if (!cd) return;
//
//     const html = cd.getData('text/html');
//     const text = cd.getData('text/plain');
//
//     // 우리가 처리할 경우 기본 paste 막음
//     if (html || text) e.preventDefault();
//
//     // ===== HTML 우선 =====
//     if (html && html.trim()) {
//         let clean = html;
//
//         if (window.DOMPurify) {
//             clean = DOMPurify.sanitize(html, {
//                 WHOLE_DOCUMENT: false,
//
//                 ALLOWED_TAGS: [
//                     // text
//                     'p','br','div','span','b','strong','i','em','u','s',
//                     'blockquote','pre','code',
//                     'h1','h2','h3','h4','h5','h6',
//                     'ul','ol','li',
//
//                     // link / table
//                     'a',
//                     'table','thead','tbody','tr','th','td',
//
//                     // media
//                     'img','figure','figcaption',
//                     'video','audio','source','track',
//                     'iframe'
//                 ],
//
//                 ALLOWED_ATTR: [
//                     // common
//                     'href','target','rel',
//                     'class','style','title',
//
//                     // img
//                     'src','alt','width','height',
//
//                     // video/audio
//                     'controls','autoplay','muted','loop','poster','preload',
//                     'playsinline',
//
//                     // iframe
//                     'allow','allowfullscreen','frameborder','referrerpolicy',
//
//                     // misc
//                     'type'
//                 ],
//
//                 FORBID_TAGS: [
//                     'script','style','meta','link',
//                     'object','embed','form','input','button'
//                 ],
//
//                 FORBID_ATTR: [
//                     'onerror','onload','onclick','onmouseover','onfocus','onblur',
//                     'onkeydown','onkeyup','onkeypress'
//                 ]
//             });
//         } else {
//             // DOMPurify 없을 때 최소 방어
//             clean = html.replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '');
//         }
//
//         // Summernote에 HTML 삽입
//         $('#content').summernote('pasteHTML', clean);
//         return;
//     }
//
//     // ===== HTML 없으면 plain text =====
//     if (text) {
//         const escaped = text
//             .replace(/&/g, '&amp;')
//             .replace(/</g, '&lt;')
//             .replace(/>/g, '&gt;')
//             .replace(/\n/g, '<br>');
//         $('#content').summernote('pasteHTML', escaped);
//     }
// };
//
// /**
//  * initSummernote()
//  * - data-summernote 속성 있는 textarea들에 summernote 적용
//  * - 각 인스턴스별 pendingFiles(본문 이미지 업로드용) 유지
//  *
//  * 사용:
//  *  - 페이지에서 DOMContentLoaded 때 initSummernote() 1번 호출
//  *  - 저장 시: window.getSummernotePayload(formEl) 사용 추천
//  */
//
// /** ====== 내부 상태: 에디터 인스턴스별 pending files ====== */
// const __snState = new WeakMap(); // element -> { pendingFiles: Map, options }
//
// /** ====== 유틸 ====== */
// function __uuidv4() {
//     if (window.crypto && crypto.randomUUID) return crypto.randomUUID();
//     return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
//         const r = Math.random() * 16 | 0;
//         const v = c === 'x' ? r : (r & 0x3 | 0x8);
//         return v.toString(16);
//     });
// }
//
// function __getExtFromFile(file) {
//     const name = file?.name || '';
//     const idx = name.lastIndexOf('.');
//     if (idx > -1) return name.substring(idx);
//     const t = (file?.type || '').toLowerCase();
//     if (t.includes('png')) return '.png';
//     if (t.includes('jpeg') || t.includes('jpg')) return '.jpg';
//     if (t.includes('webp')) return '.webp';
//     if (t.includes('gif')) return '.gif';
//     return '';
// }
//
// function __ensureState(el) {
//     if (!__snState.has(el)) {
//         __snState.set(el, { pendingFiles: new Map(), options: {} });
//     }
//     return __snState.get(el);
// }
//
// /** ====== pendingFiles <-> editor sync ====== */
// function __syncPendingFilesWithEditor($el) {
//     const el = $el.get(0);
//     const st = __ensureState(el);
//     const pendingFiles = st.pendingFiles;
//
//     const html = $el.summernote('code');
//     const temp = document.createElement('div');
//     temp.innerHTML = html;
//
//     const assetIdsInEditor = new Set(
//         Array.from(temp.querySelectorAll('img[data-asset-id]'))
//             .map(img => img.getAttribute('data-asset-id'))
//             .filter(Boolean)
//     );
//
//     for (const assetId of pendingFiles.keys()) {
//         if (!assetIdsInEditor.has(assetId)) pendingFiles.delete(assetId);
//     }
// }
//
// /** ====== 본문 이미지 업로드 (asset-id 유지) ====== */
// function __insertImageToEditor($el, file) {
//     const el = $el.get(0);
//     const st = __ensureState(el);
//     const pendingFiles = st.pendingFiles;
//
//     const assetId = __uuidv4();
//     const ext = __getExtFromFile(file);
//     const renamed = new File([file], `${assetId}${ext}`, { type: file.type });
//
//     pendingFiles.set(assetId, renamed);
//
//     const blobUrl = URL.createObjectURL(renamed);
//     const imgHtml = `<img src="${blobUrl}" data-asset-id="${assetId}" alt="" />`;
//     $el.summernote('pasteHTML', imgHtml);
// }
//
// /** ====== LH(행간) 버튼 ====== */
// function __applyLineHeight(context, value) {
//     const range = context.invoke('editor.createRange');
//     if (!range) return;
//
//     const selector = 'p, li, div, h1, h2, h3, h4, h5, h6, blockquote';
//
//     if (range.isCollapsed()) {
//         const $block = $(range.sc).closest(selector);
//         if ($block.length) $block.css('line-height', value);
//         return;
//     }
//
//     const nativeRange = range.nativeRange ? range.nativeRange() : range;
//     const startNode = nativeRange.startContainer;
//     const endNode = nativeRange.endContainer;
//
//     const $startBlock = $(startNode).closest(selector);
//     const $endBlock = $(endNode).closest(selector);
//
//     const editable = $(range.sc).closest('.note-editable').get(0);
//     if (!editable) return;
//
//     if ($startBlock.length) $startBlock.css('line-height', value);
//
//     if ($startBlock.length && $endBlock.length && !$startBlock.is($endBlock)) {
//         const blocks = $(editable).find(selector);
//         let started = false;
//         blocks.each(function () {
//             const $b = $(this);
//             if ($b.is($startBlock)) started = true;
//             if (started) $b.css('line-height', value);
//             if ($b.is($endBlock)) return false;
//         });
//     }
// }
//
// function __lineHeightButton(context) {
//     const ui = $.summernote.ui;
//
//     const saveRange = () => context.invoke('editor.saveRange');
//     const restoreRange = () => context.invoke('editor.restoreRange');
//
//     const values = ['1.0', '1.2', '1.4', '1.6', '1.8', '2.0'];
//
//     const dropdown = ui.dropdown({
//         items: values,
//         callback: function ($dropdownNode) {
//             $dropdownNode.find('a')
//                 .off('click.lineheight')
//                 .on('click.lineheight', function (e) {
//                     e.preventDefault();
//                     e.stopPropagation();
//
//                     const value = $(this).text().trim();
//                     restoreRange();
//                     context.invoke('editor.focus');
//                     __applyLineHeight(context, value);
//                 });
//         }
//     });
//
//     const button = ui.button({
//         className: 'dropdown-toggle',
//         contents: 'LH',
//         tooltip: 'Line height',
//         data: { toggle: 'dropdown', 'bs-toggle': 'dropdown' },
//         click: function () { saveRange(); }
//     });
//
//     return ui.buttonGroup([button, dropdown]).render();
// }
//
// /** ====== 메인: initSummernote ====== */
// window.initiateSummernote = function initiateSummernote(existingContent, opts = {}) {
//
//     console.log("initiating summernote...")
//
//     if (!window.jQuery || !$.fn.summernote) {
//         console.warn('Summernote not loaded');
//         return;
//     }
//
//     const defaults = {
//         selector: '[data-summernote]',
//         height: 420,
//         placeholder: '내용을 입력하세요...',
//         fontSizes: ['12', '14', '16', '18', '20', '24', '28', '32'],
//         toolbar: [
//             // ['style', ['style']],
//             ['font', ['bold', 'italic', 'underline', 'clear']],
//             ['fontsize', ['fontsize']],
//             ['para', ['ul', 'ol', 'paragraph']],
//             ['insert', ['picture', 'link', 'table']],
//             ['custom', ['lineHeight']],
//             ['view', ['codeview']]
//         ],
//         // paste 정책: app.js에 정의된 handlePasteMedia를 사용 (HTML/이미지/비디오/iframe 유지)
//         usePasteMediaHandler: true,
//     };
//
//     const cfg = Object.assign({}, defaults, opts);
//     const nodes = document.querySelectorAll(cfg.selector);
//
//     nodes.forEach((node) => {
//         const $el = $(node);
//         const st = __ensureState(node);
//         st.options = cfg;
//
//         // 이미 init 되어 있으면 destroy 후 재생성(안정패턴)
//         if ($el.next('.note-editor').length) {
//             $el.summernote('destroy');
//         }
//
//         // 초기값: 파라미터 > data-existing-content > textarea value
//         const initialHtml =
//             (existingContent != null ? existingContent : null) ??
//             node.dataset.existingContent ??
//             (node.value || '');
//
//         $el.summernote({
//             height: cfg.height,
//             placeholder: cfg.placeholder,
//             fontSizes: cfg.fontSizes,
//             toolbar: cfg.toolbar,
//             buttons: { lineHeight: __lineHeightButton },
//
//             callbacks: {
//                 onImageUpload: function (files) {
//                     if (!files || files.length === 0) return;
//                     for (const f of files) __insertImageToEditor($el, f);
//                 },
//
//                 onChange: function () {
//                     __syncPendingFilesWithEditor($el);
//                 },
//
//                 onPaste: function (e) {
//                     if (!cfg.usePasteMediaHandler) return;
//                     // ✅ 네가 app.js에 이미 만들어둔 함수라고 했으니 호출만
//                     if (typeof window.handlePasteMedia === 'function') {
//                         window.handlePasteMedia(e);
//                     } else {
//                         // fallback: 기본 동작
//                         console.warn('handlePasteMedia is not defined; paste will use default');
//                     }
//                 },
//
//                 // selection 보존 (LH 드롭다운 등에서 커서 날아가는 문제 방지)
//                 onKeyup: function () { $el.summernote('saveRange'); },
//                 onMouseup: function () { $el.summernote('saveRange'); },
//
//                 onInit: function () {
//                     // 초기 HTML 주입
//                     $el.summernote('code', initialHtml);
//                     __syncPendingFilesWithEditor($el);
//                 }
//             }
//         });
//     });
// };
//
//
// /**
//  * 저장용 헬퍼
//  * - 폼 전송 전에 editor html + pending files를 꺼내서 FormData에 넣기 좋게 반환
//  *
//  * 사용 예:
//  *  const payload = getSummernotePayload(formEl);
//  *  formData.set('content', payload.htmlByName.content);  // name="content" 기준
//  *  payload.files.forEach(f => formData.append('files', f));
//  */
// window.getSummernotePayload = function getSummernotePayload(formEl, opts = {}) {
//     if (!window.jQuery || !$.fn.summernote) {
//         return { htmlByName: {}, files: [] };
//     }
//
//     const selector = opts.selector || '[data-summernote]';
//     const htmlByName = {};
//     const files = [];
//
//     const nodes = formEl
//         ? formEl.querySelectorAll(selector)
//         : document.querySelectorAll(selector);
//
//     nodes.forEach((node) => {
//         const $el = $(node);
//
//         // HTML 추출
//         const html = $el.summernote('code');
//         const key = node.name || node.id || 'summernote';
//         htmlByName[key] = html;
//
//         // pendingFiles 추출
//         const st = __snState.get(node);
//         if (st?.pendingFiles) {
//             // 편집기에서 삭제된 이미지 정리
//             __syncPendingFilesWithEditor($el);
//
//             for (const f of st.pendingFiles.values()) {
//                 files.push(f);
//             }
//         }
//     });
//
//     return { htmlByName, files };
// };

/* app.js
 * - data-summernote 붙은 textarea 자동 초기화
 * - paste 시 미디어 유지 + sanitize(DOMPurify)
 * - 이미지 업로드: blob preview + pendingFiles 추적 (data-asset-id)
 * - line-height dropdown (LH)
 * - payload helper: getSummernotePayload()
 */

(function () {
    const W = window;

    // --------------------------
    // Utils
    // --------------------------
    function uuidv4() {
        if (W.crypto && crypto.randomUUID) return crypto.randomUUID();
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            const r = (Math.random() * 16) | 0;
            const v = c === 'x' ? r : (r & 0x3) | 0x8;
            return v.toString(16);
        });
    }

    function getExtFromFile(file) {
        const name = file?.name || '';
        const idx = name.lastIndexOf('.');
        if (idx > -1) return name.substring(idx);
        const t = (file?.type || '').toLowerCase();
        if (t.includes('png')) return '.png';
        if (t.includes('jpeg') || t.includes('jpg')) return '.jpg';
        if (t.includes('webp')) return '.webp';
        if (t.includes('gif')) return '.gif';
        return '';
    }

    function isSummernoteInitialized($el) {
        return !!($el && $el.length && $el.next('.note-editor').length);
    }

    function getEditorHtml($el) {
        if (!$el?.length) return '';
        if (!$el.summernote) return $el.val?.() ?? '';
        return $el.summernote('code');
    }

    // --------------------------
    // DOMPurify sanitize for paste
    // --------------------------
    function sanitizePastedHtml(html) {
        if (!html) return '';
        if (W.DOMPurify) {
            return W.DOMPurify.sanitize(html, {
                WHOLE_DOCUMENT: false,
                ALLOWED_TAGS: [
                    'p','br','div','span','b','strong','i','em','u','s',
                    'blockquote','pre','code',
                    'h1','h2','h3','h4','h5','h6',
                    'ul','ol','li',
                    'a',
                    'table','thead','tbody','tr','th','td',
                    'img','figure','figcaption',
                    'video','audio','source','track',
                    'iframe'
                ],
                ALLOWED_ATTR: [
                    'href','target','rel',
                    'class','style','title',
                    'src','alt','width','height',
                    'controls','autoplay','muted','loop','poster','preload','playsinline',
                    'type',
                    'allow','allowfullscreen','frameborder','referrerpolicy'
                ],
                FORBID_TAGS: ['script','meta','link','object','embed','form','input','button'],
                FORBID_ATTR: [/^on/i] // onload, onclick...
            });
        }
        // DOMPurify 없으면 최소 방어
        return html.replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, '');
    }

    // --------------------------
    // Global: handlePasteMedia
    // --------------------------
    W.handlePasteMedia = function handlePasteMedia(e, opts = {}) {
        const oe = e.originalEvent || e;
        const cd = oe.clipboardData;
        if (!cd) return;

        const html = cd.getData('text/html');
        const text = cd.getData('text/plain');

        const $editor = opts.$editor; // jQuery summernote element
        if (!$editor || !$editor.length) return;

        // html/text 있으면 브라우저 기본 paste 막고 우리가 삽입
        if (html || text) e.preventDefault();

        if (html && html.trim()) {
            const clean = sanitizePastedHtml(html);
            $editor.summernote('pasteHTML', clean);
            return;
        }

        // html이 없으면 plain text로 (줄바꿈 유지)
        if (text) {
            const escaped = text
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/\n/g, '<br>');
            $editor.summernote('pasteHTML', escaped);
        }
    };

    // --------------------------
    // Summernote instance registry
    // --------------------------
    const instances = new Map(); // key: element, value: { pendingFiles, blobUrls, api }

    function getOrCreateState(el) {
        if (instances.has(el)) return instances.get(el);
        const state = {
            pendingFiles: new Map(), // assetId -> File
            blobUrls: new Map(),     // assetId -> blobUrl
            onChange: null,
            contentHidden: null,
        };
        instances.set(el, state);
        return state;
    }

    function syncPendingFilesWithEditor($el) {
        const el = $el.get(0);
        const state = getOrCreateState(el);

        const html = getEditorHtml($el);
        const temp = document.createElement('div');
        temp.innerHTML = html;

        const assetIdsInEditor = new Set(
            Array.from(temp.querySelectorAll('img[data-asset-id]'))
                .map((img) => img.getAttribute('data-asset-id'))
                .filter(Boolean)
        );

        for (const assetId of Array.from(state.pendingFiles.keys())) {
            if (!assetIdsInEditor.has(assetId)) {
                state.pendingFiles.delete(assetId);

                const url = state.blobUrls.get(assetId);
                if (url) {
                    try { URL.revokeObjectURL(url); } catch (e) {}
                }
                state.blobUrls.delete(assetId);
            }
        }
    }

    function insertImageToEditor($el, file) {
        const el = $el.get(0);
        const state = getOrCreateState(el);

        const assetId = uuidv4();
        const ext = getExtFromFile(file);
        const renamed = new File([file], `${assetId}${ext}`, { type: file.type });

        state.pendingFiles.set(assetId, renamed);

        const blobUrl = URL.createObjectURL(renamed);
        state.blobUrls.set(assetId, blobUrl);

        const imgHtml = `<img src="${blobUrl}" data-asset-id="${assetId}" alt="" />`;
        $el.summernote('pasteHTML', imgHtml);
    }

    function destroyInstance($el) {
        const el = $el.get(0);
        const state = instances.get(el);
        if (state) {
            for (const url of state.blobUrls.values()) {
                try { URL.revokeObjectURL(url); } catch (e) {}
            }
            state.pendingFiles.clear();
            state.blobUrls.clear();
            instances.delete(el);
        }
        if (isSummernoteInitialized($el)) {
            $el.summernote('destroy');
        }
    }

    // --------------------------
    // Line Height button
    // --------------------------
    function applyLineHeight(context, value) {
        const range = context.invoke('editor.createRange');
        if (!range) return;

        const selector = 'p, li, div, h1, h2, h3, h4, h5, h6, blockquote';

        if (range.isCollapsed()) {
            const $block = W.jQuery(range.sc).closest(selector);
            if ($block.length) $block.css('line-height', value);
            return;
        }

        const nativeRange = range.nativeRange ? range.nativeRange() : range;
        const startNode = nativeRange.startContainer;
        const endNode = nativeRange.endContainer;

        const $startBlock = W.jQuery(startNode).closest(selector);
        const $endBlock   = W.jQuery(endNode).closest(selector);

        const editable = W.jQuery(range.sc).closest('.note-editable').get(0);
        if (!editable) return;

        if ($startBlock.length) $startBlock.css('line-height', value);

        if ($startBlock.length && $endBlock.length && !$startBlock.is($endBlock)) {
            const blocks = W.jQuery(editable).find(selector);
            let started = false;
            blocks.each(function () {
                const $b = W.jQuery(this);
                if ($b.is($startBlock)) started = true;
                if (started) $b.css('line-height', value);
                if ($b.is($endBlock)) return false;
            });
        }
    }

    function lineHeightButton(context, $el) {
        const ui = W.jQuery.summernote.ui;
        const saveRange = () => context.invoke('editor.saveRange');
        const restoreRange = () => context.invoke('editor.restoreRange');

        const values = ['1.0', '1.2', '1.4', '1.6', '1.8', '2.0'];

        const dropdown = ui.dropdown({
            items: values,
            callback: function ($dropdownNode) {
                $dropdownNode.find('a').off('click.lineheight').on('click.lineheight', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    const value = W.jQuery(this).text().trim();

                    restoreRange();
                    context.invoke('editor.focus');
                    applyLineHeight(context, value);

                    // line-height는 change로 안 잡힐 수 있어서 한번 sync
                    const el = $el.get(0);
                    const state = getOrCreateState(el);
                    if (state.contentHidden) state.contentHidden.value = getEditorHtml($el);
                    if (typeof state.onChange === 'function') state.onChange(getEditorHtml($el));
                });
            }
        });

        const button = ui.button({
            className: 'dropdown-toggle',
            contents: 'LH',
            tooltip: 'Line height',
            data: { toggle: 'dropdown', 'bs-toggle': 'dropdown' },
            click: saveRange
        });

        return ui.buttonGroup([button, dropdown]).render();
    }

    // --------------------------
    // Public: initSummernote
    // --------------------------
    W.initSummernote = function initSummernote(params = {}) {
        const {
            selector,
            existingContent = '',
            height = 420,
            contentHiddenSelector, // ex) '#contentHidden'
            scopeClass = 'sn-common',
            // custom hooks
            onAfterInit,
            onChange,
            onImageUpload, // override default insertImageToEditor
            // paste override (default: handlePasteMedia)
            onPaste,
        } = params;

        if (!W.jQuery) {
            console.warn('jQuery not found');
            return null;
        }

        const $el = W.jQuery(selector);
        if (!$el.length || !$el.summernote) {
            console.warn('Summernote not loaded or element not found:', selector);
            return null;
        }

        // destroy if already init
        if (isSummernoteInitialized($el)) {
            $el.summernote('destroy');
        }

        // state
        const el = $el.get(0);
        const state = getOrCreateState(el);
        state.onChange = typeof onChange === 'function' ? onChange : null;
        state.contentHidden = contentHiddenSelector
            ? document.querySelector(contentHiddenSelector)
            : null;

        $el.summernote({
            height,
            placeholder: '내용을 입력하세요...',
            fontSizes: ['12','14','16','18','20','24','28','32'],
            toolbar: [
                ['style', ['bold','italic','underline','clear']],
                ['fontsize', ['fontsize']],
                ['para', ['ul','ol','paragraph']],
                ['insert', ['link','picture','table']],
                ['custom', ['lineHeight']],
                ['view', ['codeview']]
            ],
            buttons: {
                lineHeight: function (context) { return lineHeightButton(context, $el); }
            },
            callbacks: {
                onInit: function () {
                    // wrapper class
                    $el.next('.note-editor').addClass(scopeClass);

                    $el.summernote('code', existingContent || '');

                    const html = getEditorHtml($el);
                    if (state.contentHidden) state.contentHidden.value = html;
                    if (typeof state.onChange === 'function') state.onChange(html);

                    if (typeof onAfterInit === 'function') onAfterInit($el);
                },

                onImageUpload: function (files) {
                    if (!files || !files.length) return;

                    if (typeof onImageUpload === 'function') {
                        onImageUpload(files, $el);
                        return;
                    }

                    for (const f of files) insertImageToEditor($el, f);
                },

                onChange: function (contents) {
                    syncPendingFilesWithEditor($el);

                    if (state.contentHidden) state.contentHidden.value = contents || '';
                    if (typeof state.onChange === 'function') state.onChange(contents || '');
                },

                onPaste: function (e) {
                    if (typeof onPaste === 'function') {
                        onPaste(e, $el);
                        return;
                    }
                    if (typeof W.handlePasteMedia === 'function') {
                        W.handlePasteMedia(e, { $editor: $el });
                    }
                },

                onKeyup: function () { $el.summernote('saveRange'); },
                onMouseup: function () { $el.summernote('saveRange'); }
            }
        });

        // api 반환
        const api = {
            $el,
            destroy: () => destroyInstance($el),
            getHTML: () => {
                syncPendingFilesWithEditor($el);
                return getEditorHtml($el);
            },
            setHTML: (html) => {
                $el.summernote('code', html || '');
                const h = getEditorHtml($el);
                if (state.contentHidden) state.contentHidden.value = h;
            },
            getPendingFiles: () => {
                syncPendingFilesWithEditor($el);
                return new Map(state.pendingFiles);
            }
        };

        return api;
    };

    // --------------------------
    // Public: initAllSummernotes
    // - data-summernote 붙은 textarea 전부 초기화
    // - data-existing="#someDiv" 로 초기 HTML 가져오기 가능
    // - data-hidden="#contentHidden" 로 hidden sync 가능
    // --------------------------
    W.initAllSummernotes = function initAllSummernotes(root = document) {
        if (!W.jQuery) return;
        const nodes = root.querySelectorAll('textarea[data-summernote]');
        nodes.forEach((ta) => {
            const sel = `#${ta.id}`;
            if (!ta.id) return;

            const existingSel = ta.dataset.existing;
            const hiddenSel = ta.dataset.hidden;
            const height = ta.dataset.height ? parseInt(ta.dataset.height, 10) : 420;

            let existingContent = ta.value || '';

            // existing source 우선
            if (existingSel) {
                const src = document.querySelector(existingSel);
                if (src) existingContent = src.innerHTML || '';
            }

            W.initSummernote({
                selector: sel,
                existingContent,
                height,
                contentHiddenSelector: hiddenSel || null
            });
        });
    };

    // --------------------------
    // Public: getSummernotePayload
    // - form 기반으로 content + files payload 만들기
    // --------------------------
    W.getSummernotePayload = function getSummernotePayload(options = {}) {
        const {
            editorSelector,       // '#content' or '#translatedContentEditor'
            contentHiddenSelector // '#contentHidden' (optional)
        } = options;

        if (!W.jQuery) throw new Error('jQuery not found');
        const $el = W.jQuery(editorSelector);
        if (!$el.length) throw new Error('editor not found: ' + editorSelector);

        // 최신 상태 반영
        syncPendingFilesWithEditor($el);

        const html = getEditorHtml($el);

        // hidden sync
        if (contentHiddenSelector) {
            const hidden = document.querySelector(contentHiddenSelector);
            if (hidden) hidden.value = html || '';
        }

        const el = $el.get(0);
        const state = instances.get(el);

        const files = [];
        if (state?.pendingFiles) {
            for (const f of state.pendingFiles.values()) files.push(f);
        }

        return { content: html || '', files };
    };

    // --------------------------
    // Auto init on DOMContentLoaded (optional)
    // --------------------------
    document.addEventListener('DOMContentLoaded', () => {
        // 자동 초기화 원치 않으면 주석 처리 가능
        W.initAllSummernotes(document);
    });

})();
