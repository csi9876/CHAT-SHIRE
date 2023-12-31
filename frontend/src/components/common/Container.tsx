import React from "react";

interface ContainerProps {
  fontFamily?: string;
  fontSize?: string;
  backgroundColor: string;
  text: string;
  width: string;
  height: string;
  margin: string;
  padding: string;
  border?: string;
  borderRadius: string;
  boxShadow: string;
  children?: React.ReactNode;
  display?: string;
  flexDirection?: any;
  justifyContent?: string;
  backdropFilter?: string;
  transition?: string;
}

const Container: React.FC<ContainerProps> = ({
  backgroundColor,
  text,
  width,
  height,
  margin,
  padding,
  border,
  borderRadius,
  boxShadow,
  children,
  display,
  flexDirection,
  justifyContent,
  backdropFilter,
  transition,
  fontFamily,
}) => {
  const boxStyle = {
    backgroundColor,
    width,
    height,
    margin,
    padding,
    border,
    borderRadius,
    boxShadow,
    display,
    flexDirection,
    fontFamily,
    justifyContent,
    WebkitBackdropFilter: backdropFilter,
    transition,
    "&:hover": {
      boxShadow: "-2px -2px 5px white, 2px 2px 5px #babecc",
    },
  };

  return (
    <div style={boxStyle}>
      {children}
      <p>{text}</p>
    </div>
  );
};

export default Container;
