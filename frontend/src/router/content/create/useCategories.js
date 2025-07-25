import { useState, useEffect } from "react";
import axiosInstance from "../../../lib/axios";

export const useCategories = (onCategorySelect) => {
  const [categoryTree, setCategoryTree] = useState([]);
  const [selectedCategory1, setSelectedCategory1] = useState("");
  const [selectedCategory2, setSelectedCategory2] = useState("");
  const [selectedCategory3, setSelectedCategory3] = useState("");

  useEffect(() => {
    axiosInstance.get("/api/categories/tree").then((res) => {
      setCategoryTree(res.data);
    });
  }, []);

  const category1Options = categoryTree;
  const category2Options = selectedCategory1 ? categoryTree.find((cat) => String(cat.id) === String(selectedCategory1))?.children || [] : [];
  const category3Options = selectedCategory2 ? category2Options.find((cat) => String(cat.id) === String(selectedCategory2))?.children || [] : [];

  const handleCategory1Change = (value) => {
    setSelectedCategory1(value);
    setSelectedCategory2("");
    setSelectedCategory3("");
    onCategorySelect("");
  };

  const handleCategory2Change = (value) => {
    setSelectedCategory2(value);
    setSelectedCategory3("");
    onCategorySelect("");
  };

  const handleCategory3Change = (value) => {
    setSelectedCategory3(value);
    onCategorySelect(value);
  };

  // 자동으로 최종 카테고리 ID 설정
  useEffect(() => {
    if (selectedCategory2 && category3Options.length === 0) {
      onCategorySelect(selectedCategory2);
    }
  }, [selectedCategory2, category3Options.length, onCategorySelect]);

  useEffect(() => {
    if (selectedCategory1 && category2Options.length === 0) {
      onCategorySelect(selectedCategory1);
    }
  }, [selectedCategory1, category2Options.length, onCategorySelect]);

  return {
    selectedCategory1,
    selectedCategory2,
    selectedCategory3,
    category1Options,
    category2Options,
    category3Options,
    handleCategory1Change,
    handleCategory2Change,
    handleCategory3Change,
  };
};
