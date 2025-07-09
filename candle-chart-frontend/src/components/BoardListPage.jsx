import React, {useCallback, useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

const BoardPage = ({stockId}) => {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [editingPostId, setEditingPostId] = useState(null);
    const [editTitle, setEditTitle] = useState("");
    const [editContent, setEditContent] = useState("");
    const [commentsMap, setCommentsMap] = useState({});
    const [commentInputs, setCommentInputs] = useState({});

    const navigate = useNavigate();

    const fetchComments = useCallback(async (postId) => {
        try {
            const res = await apiClient.get(`/api/boards/${postId}/comments`);
            setCommentsMap((prev) => ({...prev, [postId]: res.data.data.content || []}));
        } catch (err) {
            console.error(`댓글 조회 실패(postId=${postId}):`, err);
        }
    }, []);


    const fetchPosts = useCallback(async () => {
        setLoading(true);
        try {
            const res = await apiClient.get(`/api/stocks/${stockId}/board`);
            setPosts(res.data);
            res.data.forEach((post) => fetchComments(post.id));
        } catch (err) {
            console.error("게시글 조회 실패:", err);
            setError("게시글 조회 중 오류가 발생했습니다.");
        }
        setLoading(false);
    }, [stockId, fetchComments]);

    useEffect(() => {
        fetchPosts();
    }, [fetchPosts]);

    const handleEdit = (post) => {
        setEditingPostId(post.id);
        setEditTitle(post.title);
        setEditContent(post.content);
    };

    const handleEditSubmit = async (postId) => {
        if (!editTitle || !editContent) {
            alert("제목과 내용을 모두 입력해주세요.");
            return;
        }
        try {
            console.log("PATCH 요청 보냄:", {postId, title: editTitle, content: editContent});
            await apiClient.patch(`/api/stocks/${stockId}/board/${postId}`, {
                title: editTitle,
                content: editContent,
            });
            setEditingPostId(null);
            await fetchPosts();
        } catch (err) {
            console.error("게시글 수정 실패:", err);
            alert("게시글 수정 실패");
        }
    };


    const handleDelete = async (postId) => {
        if (!window.confirm("정말 삭제하시겠습니까?")) return;
        try {
            await apiClient.delete(`/api/stocks/${stockId}/board/${postId}`);
            fetchPosts(postId);
        } catch (err) {
            console.error("게시글 삭제 실패:", err);
            alert("게시글 삭제 실패");
        }
    };

    const handleCommentChange = (postId, value) => {
        setCommentInputs((prev) => ({...prev, [postId]: value}));
    };

    const handleCommentSubmit = async (postId) => {
        const content = commentInputs[postId];
        if (!content || content.trim() === "") {
            alert("댓글 내용을 입력해주세요.");
            return;
        }
        try {
            await apiClient.post(`/api/boards/${postId}/comments`, {content});
            setCommentInputs((prev) => ({...prev, [postId]: ""}));
            fetchComments(postId);
        } catch (err) {
            console.error("댓글 등록 실패:", err);
            alert("댓글 등록 실패");
        }
    };

    const handleCommentDelete = async (postId, commentId) => {
        if (!window.confirm("댓글을 삭제하시겠습니까?")) return;
        try {
            await apiClient.delete(`/api/boards/${postId}/comments/${commentId}`);
            fetchComments(postId);
        } catch (err) {
            console.error("댓글 삭제 실패:", err);
            alert("댓글 삭제 실패");
        }
    };

    const handleWriteClick = () => {
        navigate("/board/write");
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>게시판</h2>

            <button onClick={handleWriteClick} style={styles.button}>
                글쓰기
            </button>

            {loading && <p>로딩 중...</p>}
            {error && <p style={{color: "red"}}>{error}</p>}

            <div style={{width: "100%"}}>
                {posts.map((post) => (
                    <div key={post.id} style={styles.postCard}>
                        {editingPostId === post.id ? (
                            <>
                                <input
                                    type="text"
                                    value={editTitle}
                                    onChange={(e) => setEditTitle(e.target.value)}
                                    style={{...styles.input, marginBottom: 8}}
                                />
                                <textarea
                                    value={editContent}
                                    onChange={(e) => setEditContent(e.target.value)}
                                    rows={4}
                                    style={{...styles.input, resize: "none"}}
                                />
                                <button
                                    onClick={() => handleEditSubmit(post.id)}
                                    style={styles.button}
                                >
                                    저장
                                </button>
                                <button
                                    onClick={() => setEditingPostId(null)}
                                    style={{...styles.button, backgroundColor: "gray", marginLeft: 10}}
                                >
                                    취소
                                </button>
                            </>
                        ) : (
                            <>
                                <h3>{post.title}</h3>
                                <p>{post.content}</p>
                                <div style={{marginTop: 10}}>
                                    <button
                                        onClick={() => handleEdit(post)}
                                        style={styles.editButton}
                                    >
                                        수정
                                    </button>
                                    <button
                                        onClick={() => handleDelete(post.id)}
                                        style={styles.deleteButton}
                                    >
                                        삭제
                                    </button>
                                </div>
                            </>
                        )}

                        <div style={{marginTop: 20, paddingLeft: 10, borderLeft: "3px solid #ddd"}}>
                            <h4>댓글</h4>
                            {(commentsMap[post.id] || []).map((comment) => (
                                <div key={comment.id} style={styles.commentCard}>
                                    <p style={{margin: 0}}>{comment.content}</p>
                                    <button
                                        onClick={() => handleCommentDelete(post.id, comment.id)}
                                        style={styles.commentDeleteBtn}
                                    >
                                        삭제
                                    </button>
                                </div>
                            ))}
                            <textarea
                                placeholder="댓글을 입력하세요"
                                value={commentInputs[post.id] || ""}
                                onChange={(e) => handleCommentChange(post.id, e.target.value)}
                                rows={2}
                                style={styles.commentInput}
                            />
                            <button
                                onClick={() => handleCommentSubmit(post.id)}
                                style={styles.commentSubmitBtn}
                            >
                                댓글 작성
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

const styles = {
    container: {
        maxWidth: 600,
        margin: "30px auto",
        padding: "40px 20px",
        fontFamily: "sans-serif",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
    },
    title: {
        fontSize: 24,
        fontWeight: "bold",
        marginBottom: 20,
    },
    button: {
        marginBottom: 20,
        padding: "10px 20px",
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold",
        fontSize: 14,
    },
    editButton: {
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        marginRight: 8,
        padding: "4px 10px",
        fontSize: 12,
        borderRadius: 6,
        cursor: "pointer",
        fontWeight: "bold",
        height: 28,
        lineHeight: "20px",
    },
    deleteButton: {
        backgroundColor: "#E44343",
        color: "#fff",
        border: "none",
        padding: "4px 10px",
        fontSize: 12,
        borderRadius: 6,
        cursor: "pointer",
        height: 28,
        lineHeight: "20px",
    },
    input: {
        width: "100%",
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
    },
    postCard: {
        border: "1px solid #ccc",
        borderRadius: 8,
        padding: 16,
        marginBottom: 15,
        backgroundColor: "#fff",
    },
    commentCard: {
        borderBottom: "1px solid #eee",
        padding: "5px 0",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },
    commentDeleteBtn: {
        backgroundColor: "#E44343",
        border: "none",
        color: "white",
        borderRadius: 4,
        cursor: "pointer",
        padding: "2px 8px",
        fontSize: 12,
        height: 24,
        lineHeight: "18px",
    },
    commentInput: {
        width: "100%",
        padding: 8,
        marginTop: 10,
        fontSize: 14,
        borderRadius: 6,
        border: "1px solid #ccc",
        resize: "none",
    },
    commentSubmitBtn: {
        marginTop: 8,
        padding: "8px 16px",
        backgroundColor: "#4461F2",
        color: "white",
        border: "none",
        borderRadius: 6,
        cursor: "pointer",
        fontWeight: "bold",
    },
};

export default BoardPage;
