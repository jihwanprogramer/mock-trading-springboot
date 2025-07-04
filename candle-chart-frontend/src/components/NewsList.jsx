import React, {useState} from "react";
import apiClient from "./api";

const NewsList = () => {
    const [keyword, setKeyword] = useState("");
    const [articles, setArticles] = useState([]);

    const handleSearch = async () => {
        try {
            const res = await apiClient.get("/api/v1/news/search", {
                params: {keyword}
            });
            setArticles(res.data.articles);
        } catch (err) {
            console.error("뉴스 검색 실패:", err);
            alert("뉴스를 불러오지 못했습니다.");
        }
    };

    return (
        <div style={styles.container}>
            <img src="/logo.png" alt="logo" style={styles.logo}/>
            <h2 style={styles.title}> 뉴스 검색</h2>
            <p style={styles.subtitle}>원하는 키워드로 뉴스를 검색해보세요.</p>

            <div style={styles.form}>
                <input
                    type="text"
                    placeholder="검색어 입력"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    style={styles.input}
                />
                <button onClick={handleSearch} style={styles.button}>검색</button>
            </div>

            <ul style={styles.articleList}>
                {articles.map((news, idx) => (
                    <li key={idx} style={styles.articleItem}>
                        <a href={news.link} target="_blank" rel="noopener noreferrer" style={styles.articleTitle}>
                            {news.title.replace(/<[^>]*>?/g, "")}
                        </a>
                        <p style={styles.articleDesc}>
                            {news.description.replace(/<[^>]*>?/g, "")}
                        </p>
                    </li>
                ))}
            </ul>
        </div>
    );
};

const styles = {
    container: {
        maxWidth: 500,
        margin: '30px auto',
        padding: '40px 20px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        fontFamily: 'sans-serif',
        border: '1px solid #ddd',
        borderRadius: 10,
        backgroundColor: '#fafafa',
    },

    logo: {
        width: 60,
        height: 60,
        marginBottom: 20,
    },
    title: {
        fontSize: 22,
        fontWeight: 'bold',
        marginBottom: 5,
    },
    subtitle: {
        fontSize: 14,
        color: '#666',
        marginBottom: 30,
    },
    form: {
        width: '100%',
        display: 'flex',
        gap: '10px',
        marginBottom: 30,
    },
    input: {
        flex: 1,
        padding: '12px 15px',
        fontSize: 14,
        border: '1px solid #ddd',
        borderRadius: 10,
        outline: 'none',
    },
    button: {
        padding: '12px 20px',
        fontSize: 14,
        backgroundColor: '#4461F2',
        color: '#fff',
        border: 'none',
        borderRadius: 10,
        cursor: 'pointer',
        fontWeight: 'bold',
    },
    articleList: {
        width: '100%',
        listStyleType: 'none',
        padding: 0,
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
    },
    articleItem: {
        border: '1px solid #ddd',
        borderRadius: 10,
        padding: '15px',
        backgroundColor: '#f9f9f9',
    },
    articleTitle: {
        color: '#4461F2',
        fontSize: 16,
        fontWeight: 'bold',
        textDecoration: 'none',
        marginBottom: '5px',
        display: 'block',
    },
    articleDesc: {
        fontSize: 14,
        color: '#555',
        marginTop: '5px',
    },
};

export default NewsList;
