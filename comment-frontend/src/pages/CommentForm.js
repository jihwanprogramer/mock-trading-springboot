import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';

const CommentForm = () => {
    const { boardId } = useParams();
    const [content, setContent] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/boards/${boardId}/comments`, { content });
            navigate(`/boards/${boardId}/comments`);
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <textarea value={content} onChange={(e) => setContent(e.target.value)} />
            <button type="submit">작성</button>
        </form>
    );
};

export default CommentForm;
