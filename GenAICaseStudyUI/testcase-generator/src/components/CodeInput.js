import React from 'react';

const CodeInput = ({ value, onChange }) => {
  return (
    <textarea
      className="code-input"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder="Enter your code here..."
    />
  );
};

export default CodeInput;
