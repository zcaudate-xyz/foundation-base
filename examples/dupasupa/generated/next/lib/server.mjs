import * as postgres from 'pg'

import {createClient} from '@supabase/supabase-js'

export async function query(sql,values){
  if(!process.env.DUPASUPA_DATABASE_URL){
    throw new Error(
      "Start Dupasupa with make serve to prepare its local database."
    );
  }
  let client = new postgres.Client({"connectionString":process.env.DUPASUPA_DATABASE_URL});
  try{
    await client.connect();
    let result = await client.query(sql,values);
    return result.rows[0]["value"];
  }
  catch(error){
    throw error;
  }
  finally{
    await client.end();
  }
}

export async function verifiedUser(request){
  let header = request.headers.get("authorization");
  if(!header || !header.startsWith("Bearer ")){
    throw Object.assign(new Error("Sign in to continue."),{"status":401});
  }
  let client = createClient(
    process.env.DUPASUPA_SUPABASE_URL,
    process.env.DUPASUPA_SUPABASE_KEY,
    {"auth":{"persistSession":false,"autoRefreshToken":false}}
  );
  let response = await client.auth.getUser(header.slice(7));
  if(response.error || !response.data.user){
    throw Object.assign(new Error("Sign in again."),{"status":401});
  }
  return response.data.user;
}

export function failure(error){
  let invalid = new RegExp("^(22|23)|^P0001$").test(error.code || "");
  let status = error.status || (invalid ? 400 : 500);
  return Response.json(
    {"error":(status < 500) ? error.message : "Request failed."},
    {"status":status}
  );
}

export async function GET(request){
  try{
    let user = await verifiedUser(request);
    return Response.json(await query(
      "SELECT dupasupa_rpc.list_owned_campaigns($1::uuid) AS value",
      [user.id]
    ));
  }
  catch(error){
    return failure(error);
  }
}

export async function POST(request){
  try{
    let user = await verifiedUser(request);
    let values = await request.json();
    if(values.action == "create"){
      return Response.json(await query(
        "SELECT dupasupa_rpc.create_owned_campaign($1::uuid,$2::text) AS value",
        [user.id,values.title]
      ),{"status":201});
    }
    if(values.action && (values.action != "save")){
      throw Object.assign(new Error("Unknown campaign action."),{"status":400});
    }
    await query(
      "SELECT dupasupa_rpc.save_owned_campaign($1::uuid,$2::uuid,$3::text) AS value",
      [user.id,values.id,values.title]
    );
    return Response.json(await query(
      "SELECT dupasupa_rpc.list_owned_campaigns($1::uuid) AS value",
      [user.id]
    ));
  }
  catch(error){
    return failure(error);
  }
}

export async function GET_PROFILE(request){
  try{
    let user = await verifiedUser(request);
    let profile = await query(
      "SELECT dupasupa_rpc.ret_profile($1::uuid) AS value",
      [user.id]
    );
    if(!profile){
      throw Object.assign(new Error("Profile not found."),{"status":404});
    }
    return Response.json(profile);
  }
  catch(error){
    return failure(error);
  }
}

export async function POST_PROFILE(request){
  try{
    let user = await verifiedUser(request);
    let values = await request.json();
    return Response.json(await query(
      "SELECT dupasupa_rpc.save_profile($1::uuid,$2::text,$3::text,$4::text) AS value",
      [user.id,values.handle,values.display_name,values.bio]
    ));
  }
  catch(error){
    return failure(error);
  }
}

export function GET_CONFIG(){
  if(!process.env.DUPASUPA_SUPABASE_URL || !process.env.DUPASUPA_SUPABASE_KEY){
    return Response.json({
      "error":"Start Dupasupa with make serve to configure local Auth."
    },{"status":503});
  }
  return Response.json({
    "url":process.env.DUPASUPA_SUPABASE_URL,
    "key":process.env.DUPASUPA_SUPABASE_KEY,
    "accounts":(process.env.NODE_ENV == "development") ? JSON.parse(process.env.DUPASUPA_DEV_ACCOUNTS || "[]") : []
  });
}

export default {
  ["GET"]:GET,
  ["POST"]:POST,
  ["GET_PROFILE"]:GET_PROFILE,
  ["POST_PROFILE"]:POST_PROFILE,
  ["GET_CONFIG"]:GET_CONFIG
}