import React, { useState } from 'react';
import './App.css';
import CodeInput from './components/CodeInput'; 
import TestCaseDisplay from './components/TestCaseDisplay'; 

function App() {
  const [inputCode, setInputCode] = useState('');
  const [testCases, setTestCases] = useState('');
  const [error, setError] = useState('');

  const handleUploadCode = () => {
    // Clear previous error and test cases
    setError('');
    setTestCases('');

    fetch('http://localhost:8080/generateTestCases', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ code: inputCode }),
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to generate test cases');
        }
        return response.json();
      })
      .then(data => {
        if (data.error) {
          setError(data.error);
        } else {
          setTestCases(data.testCases);
        }
      })
      .catch(error => {
        console.error('Error generating test cases:', error);
        setError('Failed to generate test cases');
      });
  };

  return (
    <div className="App">
      <h1>Test Case Generator</h1>
      <div className="container">
        <CodeInput value={inputCode} onChange={setInputCode} />
        <button onClick={handleUploadCode}>Generate Test Cases</button>
        {error && <div className="error-message">{error}</div>}
        {testCases && <TestCaseDisplay testCases={testCases} />}
      </div>
    </div>
  );
}

export default App;
