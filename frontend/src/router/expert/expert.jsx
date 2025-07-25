import { Routes, Route } from "react-router-dom";
import ExpertForm from "./register/register.jsx";
import Portfolio from "./portfolio/portfolio.jsx";
import Profile from "./profile/profile.jsx";
import PortfolioCreate from "./portfolio/portfolioCreate.jsx";
import PortfolioEdit from "./portfolio/portfolioEdit.jsx";
import EditProfile from "./register/edit/editProfile.jsx";

export default function Expert() {
  return (
    <>
      <Routes>
        <Route path="/register" element={<ExpertForm />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/profile/edit" element={<EditProfile />} />
        <Route path="/portfolio/:id" element={<Portfolio />} />
        <Route path="/portfolio/create" element={<PortfolioCreate />} />
        <Route path="/portfolio/edit" element={<PortfolioEdit />} />
      </Routes>
    </>
  );
}
