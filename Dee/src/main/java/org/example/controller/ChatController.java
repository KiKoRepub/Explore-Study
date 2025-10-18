package org.example.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {


    @Autowired
    ChatClient chatClient;
}

/*


const handleDownload = () => {
  downloading.value = true;
  const ids = multipleSelection.value.map((item: { id: any }) => item.id).join();
  const titleText = multipleSelection.value.length == 1 ? `${multipleSelection.value[0].reportsName}.pdf` : '信用报告.zip';
  downloadReports({ ids }).then(res => {
    // 兼容后端可能返回 直链URL（浏览器会预览）或 直接的Blob
    const triggerDownload = (blob: Blob) => {
      const objectUrl = window.URL.createObjectURL(blob);
      // const objectUrl = 'https://mpxygj.credit100.com/xhxyqy/upload/xygj/2025-09-25/48ad3abe0f134192b1fbbd130bf4f0de.pdf';

      const link = document.createElement('a');
      link.href = objectUrl;
      link.setAttribute('download', titleText);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(objectUrl);
    };

    const isBlob = res instanceof Blob;
    // 一些请求库会把blob包在 data 里
    const maybeBlob = (res as any)?.data instanceof Blob ? (res as any).data : null;

    // if (isBlob || maybeBlob) {
    //   triggerDownload((maybeBlob || res) as Blob);
    //   downloading.value = false;
    //   return;
    // }

    // 如果是后端返回的直链（可能会被浏览器在线预览），改为先取为Blob再下载
    let possibleUrl: string | undefined = typeof res === 'string'
      ? res
      : typeof (res as any)?.url === 'string'
        ? (res as any).url
        : undefined;
      // possibleUrl = 'https://mpxygj.credit100.com/xhxyqy/upload/xygj/2025-09-25/48ad3abe0f134192b1fbbd130bf4f0de.pdf';
    if (possibleUrl) {
      fetch(possibleUrl)
        .then(r => r.blob())
        .then(blob => {
          console.log('blob:', blob)
          triggerDownload(blob);
          downloading.value = false;
        })
        .catch(() => {
          downloading.value = false;
        });
      return;
    }

    // 无法识别的返回结构，尝试兜底：把 res 包装为Blob后下载
    try {
      const blob = new Blob([res as any]);
      triggerDownload(blob);
    } catch (err) {
      // 忽略，交由 catch 处理 loading 状态
    }
    downloading.value = false;
  }).catch(e => {
    downloading.value = false;
  });
};

 */