import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axiosConfig';

const CommentList = () => {
    const { boardId } = useParams();
    const [comments, setComments] = useState([]);

    useEffect(() => {
        api.get(`/boards/${boardId}/comments`)
            .then(res => setComments(res.data.data.content || []))
            .catch(err => console.error(err));
    }, [boardId]);

    return (
        <div>
            <h2>댓글 목록</h2>
            <ul>
                {comments.map(comment => (
                    <li key={comment.id}>{comment.content}</li>
                ))}
            </ul>
            <Link to={`/boards/${boardId}/comments/new`}>댓글 작성</Link>
        </div>
    );
};

export default CommentList;
