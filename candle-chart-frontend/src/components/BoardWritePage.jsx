import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import apiClient from "./api";

const BoardWritePage = ({stockId}) => {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!title || !content) {
            alert("제목과 내용을 모두 입력해주세요.");
            return;
        }
        try {
            await apiClient.post(`/api/stocks/${stockId}/board`, {title, content});
            navigate("/board");
        } catch (err) {
            alert("글 작성 실패");
        }
    };

    const handleCancel = () => {
        navigate("/board");
    };

    return (
        <div style={styles.container}>
            <h2 style={styles.title}>글쓰기</h2>
            <form onSubmit={handleSubmit} style={styles.form}>
                <input
                    type="text"
                    placeholder="제목"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    style={styles.input}
                />
                <textarea
                    placeholder="내용"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    rows={6}
                    style={{...styles.input, resize: "none"}}
                />
                <div style={styles.buttons}>
                    <button type="submit" style={styles.button}>
                        등록
                    </button>
                    <button
                        type="button"
                        onClick={handleCancel}
                        style={{
                            ...styles.button,
                            backgroundColor: "gray",
                            marginLeft: 10,
                        }}
                    >
                        취소
                    </button>
                </div>
            </form>
        </div>
    );
};

const styles = {
    container: {
        maxWidth: 600,
        margin: "30px auto",
        padding: "40px 20px",
        fontFamily: "sans-serif",
        border: "1px solid #ddd",
        borderRadius: 10,
        backgroundColor: "#fafafa",
    },
    title: {fontSize: 24, fontWeight: "bold", marginBottom: 20},
    form: {display: "flex", flexDirection: "column", gap: 10},
    input: {
        width: "100%",
        padding: 12,
        fontSize: 16,
        borderRadius: 10,
        border: "1px solid #ddd",
        outline: "none",
    },
    buttons: {marginTop: 20, display: "flex"},
    button: {
        padding: "10px 20px",
        backgroundColor: "#4461F2",
        color: "#fff",
        border: "none",
        borderRadius: 10,
        cursor: "pointer",
        fontWeight: "bold",
        fontSize: 14,
    },
};

export default BoardWritePage;
