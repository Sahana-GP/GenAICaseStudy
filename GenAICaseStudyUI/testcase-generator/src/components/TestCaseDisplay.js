import React from 'react';

const TestCaseDisplay = ({ testCases }) => {
  const testCaseStyle = {
    border: '1px solid #ccc',
    borderRadius: '5px',
    padding: '10px',
    backgroundColor: '#f8f9fa',
    whiteSpace: 'pre-wrap',
    maxHeight: '200px',
    overflowY: 'auto',
  };

  return (
    <div className="test-case-display">
      <h2>Generated Test Cases</h2>
      <div style={testCaseStyle}>
        <pre>{testCases}</pre>
      </div>
    </div>
  );
};

export default TestCaseDisplay;
