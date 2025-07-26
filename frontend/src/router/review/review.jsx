import React, { useState, useEffect } from 'react';
import axios from 'axios';

function ReviewPage({ matchingId, revieweeId }) {
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState('');
    const [files, setFiles] = useState([]);
    const [imageOrders, setImageOrders] = useState([]);
    const [reviewerId, setReviewerId] = useState(null);
    const [loadingUser, setLoadingUser] = useState(true);

    const apiBase = 'http://localhost:8080';

    // 현재 로그인된 사용자 정보 조회 (쿠키 기반 JWT)
    useEffect(() => {
        setLoadingUser(true);
        axios.get(`${apiBase}/api/me`, { withCredentials: true })
            .then(response => {
                const id = response.data.id || response.data.memberId;
                setReviewerId(id);
            })
            .catch(error => {
                console.error('유저 정보 조회 실패:', error.response || error);
                alert('유저 정보 조회에 실패했습니다. 다시 로그인 해주세요.');
            })
            .finally(() => setLoadingUser(false));
    }, []);

    const handleFileChange = (e) => {
        const selected = Array.from(e.target.files);
        if (selected.length > 5) {
            alert('최대 5장의 이미지만 첨부할 수 있습니다.');
            return;
        }
        setFiles(selected);
        setImageOrders(selected.map((_, idx) => idx));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (loadingUser) {
            alert('유저 정보를 불러오는 중입니다. 잠시만 기다려주세요.');
            return;
        }
        if (!reviewerId) {
            alert('유효한 사용자 정보가 없습니다. 다시 로그인 해주세요.');
            return;
        }
        if (!comment.trim()) {
            alert('댓글을 입력해주세요.');
            return;
        }

        const formData = new FormData();
        formData.append('matchingId', matchingId);
        formData.append('reviewerId', reviewerId);
        formData.append('revieweeId', revieweeId);
        formData.append('rating', rating);
        formData.append('comment', comment);
        files.forEach(file => formData.append('images', file));
        imageOrders.forEach(order => formData.append('imageOrders', order));

        try {
            const response = await axios.post(
                `${apiBase}/api/reviews`,
                formData,
                { withCredentials: true }
            );
            console.log('리뷰 등록 성공:', response.data);
            alert('리뷰가 성공적으로 등록되었습니다!');
            setRating(5);
            setComment('');
            setFiles([]);
            setImageOrders([]);
        } catch (error) {
            console.error('리뷰 등록 실패:', error.response || error);
            const msg = error.response?.data?.message || '리뷰 등록에 실패했습니다.';
            alert(msg);
        }
    };

    if (loadingUser) {
        return <div>유저 정보를 불러오는 중입니다...</div>;
    }

    return (
        <form onSubmit={handleSubmit} encType="multipart/form-data">
            <div>
                <label>평점:</label>
                <input
                    type="number"
                    min={1}
                    max={5}
                    value={rating}
                    onChange={e => setRating(Number(e.target.value))}
                    required
                />
            </div>
            <div>
                <label>댓글:</label>
                <textarea
                    value={comment}
                    onChange={e => setComment(e.target.value)}
                    required
                />
            </div>
            <div>
                <label>이미지 첨부 (최대 5장):</label>
                <input
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleFileChange}
                />
            </div>
            <button type="submit">
                리뷰 작성
            </button>
        </form>
    );
}

export default ReviewPage;
