"use client";
import React from 'react'

export var AuthContext = React.createContext(null);

export function useAuth(){
  return React.useContext(AuthContext);
}

export default {["AuthContext"]:AuthContext,["useAuth"]:useAuth}