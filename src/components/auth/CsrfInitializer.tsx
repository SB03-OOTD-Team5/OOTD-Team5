import {useEffect} from "react";
import {getCsrfToken} from "@/lib/api";

export default function CsrfInitializer() {
  useEffect(() => {
    getCsrfToken();
  }, []);
  return null;
}