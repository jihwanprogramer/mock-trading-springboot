import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import CommentList from './pages/CommentList';
import CommentForm from './pages/CommentForm';
import './App.css';

function App() {
  return (
      <Router>
        <Routes>
          <Route path="/boards/:boardId/comments" element={<CommentList />} />
          <Route path="/boards/:boardId/comments/new" element={<CommentForm />} />
        </Routes>
      </Router>
  );
}

export default App;