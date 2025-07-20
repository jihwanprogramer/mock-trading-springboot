import React, {useState} from "react";
import apiClient from "./api";

export default function PasswordInput() {
    const [password, setPassword] = useState("");

    const handleSubmit = async () => {
        try {
            await apiClient.post("/api/v1/accounts/verify-password", {password});
            alert("비밀번호 확인 완료");
        } catch (err) {
            console.error("비밀번호 확인 실패:", err);
            alert("비밀번호가 일치하지 않습니다.");
        }
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen px-4 bg-white">
            <img src="/logo.png" alt="logo" className="w-16 h-16 mb-4"/>
            <h1 className="text-xl font-bold mb-1">Mockstalk</h1>
            <p className="text-gray-500 mb-6">계좌 비밀번호를 입력해주세요.</p>

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full max-w-xs border rounded-md p-3 mb-6"
            />
            <button
                onClick={handleSubmit}
                className="bg-indigo-600 text-white w-full max-w-xs p-3 rounded-md font-semibold"
            >
                입력
            </button>
        </div>
    );
}