import React from "react";
import { Plus, Trash2 } from "lucide-react";

export const InputGroup = ({ label, icon: Icon, children, className = "" }) => (
  <div className={`space-y-3 ${className}`}>
    <label className="flex items-center gap-2 text-gray-700 font-medium text-sm">
      {Icon && <Icon size={16} className="text-indigo-500" />}
      {label}
    </label>
    {children}
  </div>
);

export const ModernInput = ({ className = "", ...props }) => (
  <input
    {...props}
    className={`w-full px-4 py-3 bg-white/80 backdrop-blur-sm border border-gray-200 rounded-xl \
      focus:outline-none focus:ring-2 focus:ring-indigo-500/40 focus:border-indigo-500 \
      transition-all duration-200 hover:shadow-md placeholder:text-gray-400 ${className}`}
  />
);

export const ModernSelect = ({ className = "", children, ...props }) => (
  <select
    {...props}
    className={`w-full px-4 py-3 bg-white/80 backdrop-blur-sm border border-gray-200 rounded-xl \
      focus:outline-none focus:ring-2 focus:ring-indigo-500/40 focus:border-indigo-500 \
      transition-all duration-200 hover:shadow-md appearance-none cursor-pointer ${className}`}>
    {children}
  </select>
);

export const ModernTextarea = ({ className = "", ...props }) => (
  <textarea
    {...props}
    className={`w-full px-4 py-3 bg-white/80 backdrop-blur-sm border border-gray-200 rounded-xl \
      focus:outline-none focus:ring-2 focus:ring-indigo-500/40 focus:border-indigo-500 \
      transition-all duration-200 hover:shadow-md resize-none placeholder:text-gray-400 ${className}`}
  />
);

export const AddButton = ({ onClick, children }) => (
  <button
    type="button"
    onClick={onClick}
    className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-indigo-600 \
      hover:text-indigo-700 hover:bg-indigo-50 rounded-lg transition-all duration-200">
    <Plus size={14} />
    {children}
  </button>
);

export const DeleteButton = ({ onClick }) => (
  <button
    type="button"
    onClick={onClick}
    className="p-2 text-red-500 hover:text-red-600 hover:bg-red-50 rounded-lg \
      transition-all duration-200 group">
    <Trash2 size={16} className="group-hover:scale-110 transition-transform" />
  </button>
);
