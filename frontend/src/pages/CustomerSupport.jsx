import { useUserInfoStore } from "../store/userInfo";
import UserSupport from "../components/customer/UserSupport";
import AdminSupport from "../components/customer/AdminSupport";

const CustomerSupport = () => {
    const { userInfo } = useUserInfoStore();

    if (!userInfo) {
        return <p>로그인이 필요합니다.</p>;
    }

    if (userInfo.role === "ADMIN") {
        return <AdminSupport />;
    }

    return <UserSupport />;
};

export default CustomerSupport;
