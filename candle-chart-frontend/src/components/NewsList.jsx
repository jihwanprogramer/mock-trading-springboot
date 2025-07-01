import React, {useState} from "react";
import axios from "axios";

const NewsList = () => {
    const [keyword, setKeyword] = useState("");
    const [articles, setArticles] = useState([]);

    const handleSearch = async () => {
        try {
            const res = await axios.get("/api/v1/news/search", {
                params: {keyword}
            });
            setArticles(res.data.articles);
        } catch (err) {
            console.error("뉴스 검색 실패:", err);
            alert("뉴스를 불러오지 못했습니다.");
        }
    };

    return (
        <div className="p-4">
            <h2 className="text-xl font-bold mb-2">뉴스 검색</h2>
            <input
                type="text"
                placeholder="검색어 입력"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="border p-2 mr-2"
            />
            <button onClick={handleSearch} className="bg-blue-500 text-white px-4 py-2 rounded">
                검색
            </button>

            <ul className="mt-4 space-y-2">
                {articles.map((news, idx) => (
                    <li key={idx} className="border p-3 rounded">
                        <a href={news.link} target="_blank" rel="noopener noreferrer"
                           className="text-blue-700 font-semibold">
                            {news.title.replace(/<[^>]*>?/g, "")}
                        </a>
                        <p className="text-sm text-gray-600">{news.description.replace(/<[^>]*>?/g, "")}</p>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default NewsList;
