import React from 'react';
import { X, Facebook, Link, Copy } from 'lucide-react';

const ShareModal = ({ isOpen, onClose, content }) => {
  const handleShare = (platform) => {
    const url = window.location.href;
    const title = content?.title || '서비스';

    switch(platform) {
      case 'facebook':
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`);
        break;
      case 'naver':
        window.open(`https://share.naver.com/web/shareView?url=${encodeURIComponent(url)}&title=${encodeURIComponent(title)}`);
        break;
      case 'band':
        window.open(`https://band.us/plugin/share?body=${encodeURIComponent(title + ' ' + url)}`);
        break;
      case 'copy':
        navigator.clipboard.writeText(url).then(() => {
          alert('링크가 복사되었습니다!');
        }).catch(() => {
          // fallback for older browsers
          const textArea = document.createElement('textarea');
          textArea.value = url;
          document.body.appendChild(textArea);
          textArea.select();
          document.execCommand('copy');
          document.body.removeChild(textArea);
          alert('링크가 복사되었습니다!');
        });
        break;
    }
    onClose();
  };

  const handleKeyDown = React.useCallback((e) => {
    if (e.key === 'Escape') {
      onClose();
    }
  }, [onClose]);

  React.useEffect(() => {
    if (isOpen) {
      document.addEventListener('keydown', handleKeyDown);
      return () => {
        document.removeEventListener('keydown', handleKeyDown);
      };
    }
  }, [handleKeyDown, isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/10 flex items-center justify-center z-50">

      <div className="bg-white rounded-lg p-6 w-96 max-w-md">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-semibold text-gray-900">공유하기</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 공유 옵션들 */}
        <div className="flex justify-between gap-4">
          {/* 페이스북 */}
          <button
            onClick={() => handleShare('facebook')}
            className="flex flex-col items-center p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center mb-1">
              <Facebook className="w-5 h-5 text-white" />
            </div>
            <span className="text-xs font-medium text-gray-700">페이스북</span>
          </button>

          {/* 네이버 */}
          <button
            onClick={() => handleShare('naver')}
            className="flex flex-col items-center p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center mb-1">
              <span className="text-white font-bold text-xs">blog</span>
            </div>
            <span className="text-xs font-medium text-gray-700">네이버</span>
          </button>

          {/* 밴드 */}
          <button
            onClick={() => handleShare('band')}
            className="flex flex-col items-center p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="w-10 h-10 bg-green-400 rounded-full flex items-center justify-center mb-1">
              <span className="text-white font-bold text-sm">b</span>
            </div>
            <span className="text-xs font-medium text-gray-700">밴드</span>
          </button>

          {/* 링크 복사 */}
          <button
            onClick={() => handleShare('copy')}
            className="flex flex-col items-center p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div className="w-10 h-10 bg-gray-500 rounded-full flex items-center justify-center mb-1">
              <Copy className="w-5 h-5 text-white" />
            </div>
            <span className="text-xs font-medium text-gray-700">링크 복사</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShareModal; 